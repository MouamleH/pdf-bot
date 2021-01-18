package me.mouamle.bot.pdf.bots;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.Application;
import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.service.ConcurrentCache;
import me.mouamle.bot.pdf.service.PDFTasks;
import me.mouamle.bot.pdf.service.RateLimiter;
import me.mouamle.bot.pdf.service.UserDataService;
import me.mouamle.bot.pdf.util.BotUtil;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.mouamle.bot.pdf.BotMessage.*;

@Slf4j
@SuppressWarnings("rawtypes")
public class PDFBot extends AbstractWebhookBot {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(64);

    private final RateLimiter<Integer> buttonsRateLimiter;
    private final RateLimiter<Integer> botActionsRateLimiter;

    private final ConcurrentCache<Integer, Boolean> channelMembersCache = new ConcurrentCache<>(
            "channel-members",
            Duration.ofSeconds(2).toMillis(),
            Duration.ofSeconds(1).toMillis(),
            1024
    );

    private final UserDataService<Integer, String> userImages;

    public PDFBot(BotData botData) {
        super(botData);
        buttonsRateLimiter = RateLimiter.builder()
                .name("user-actions")
                .enableLogging(true)
                .size(1024 * 10)
                .maxAttempts(1)
                .cleanUp(2)
                .ttl(6)
                .build();

        botActionsRateLimiter = RateLimiter.builder()
                .name("bot-actions")
                .size(1024)
                .ttl(4)
                .build();
        userImages = new UserDataService<>("user-images", 32);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        final Message message = update.getMessage();
        if (update.hasMessage()) {
            if (!isChannelMember(message.getFrom())) {
                return BotUtil.buildMessage(message.getFrom(), ERROR_MUST_JOIN)
                        .setReplyMarkup(KeyboardUtils.buildJoinKeyboard());
            }
            if (message.hasText()) {
                return handleTextMessage(message, message.getText());
            } else if (message.hasPhoto()) {
                return handlePhotoMessage(message);
            }
        } else if (update.hasCallbackQuery()) {
            final CallbackQuery callbackQuery = update.getCallbackQuery();
            return handleCallbackQuery(callbackQuery);
        }

        return null;
    }

    private BotApiMethod handleCallbackQuery(CallbackQuery callbackQuery) {
        final String callbackQueryId = callbackQuery.getId();
        final User from = callbackQuery.getFrom();
        final String languageCode = from.getLanguageCode();
        final Integer fromId = from.getId();

        if (!buttonsRateLimiter.action(fromId)) {
            return BotUtil.buildAnswer(languageCode, ERROR_NO_SPAM, callbackQueryId);
        }

        if (callbackQuery.getData().contains("build-imgs")) {
            return handleBuildImages(callbackQueryId, from, languageCode, fromId);
        } else if (callbackQuery.getData().contains("clear-imgs")) {
            return handleClearImages(callbackQueryId, languageCode, fromId);
        }
        return BotUtil.buildAnswer(languageCode, ERROR_GENERIC_ERROR, callbackQueryId);
    }

    private AnswerCallbackQuery handleClearImages(String callbackQueryId, String languageCode, Integer fromId) {
        this.userImages.clearUserImages(fromId);
        return BotUtil.buildAnswer(languageCode, MSG_IMAGES_CLEARED, callbackQueryId);
    }

    private AnswerCallbackQuery handleBuildImages(String callbackQueryId, User from, String languageCode, Integer fromId) {
        final Queue<String> images = this.userImages.get(fromId);

        if (images.isEmpty()) {
            return BotUtil.buildAnswer(languageCode, MSG_NO_IMAGES, callbackQueryId);
        }

        executor.schedule(() -> {
            final Queue<String> usrImages = this.userImages.get(fromId);
            PDFTasks.generatePDF(this, fromId, isAdmin(fromId), usrImages, file -> {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setDocument(file);
                sendDocument.setChatId(String.valueOf(fromId));
                sendDocument.setCaption(MSG_FILE_RENAME.formatted(languageCode) + "\n" + MSG_N_IMAGES.formatted(languageCode, usrImages.size()));

                try {
                    execute(sendDocument);
                } catch (TelegramApiException e) {
                    log.error("Could not send document to user {}, msg: {}", fromId, e.getMessage());
                }

                boolean deleted = file.delete();
                if (!deleted) {
                    log.error("Could not delete file {}", file.getName());
                }
                this.userImages.clearUserImages(fromId);
            }, error -> {
                try {
                    execute(BotUtil.buildMessage(from, error));
                } catch (TelegramApiException e) {
                    log.error("Could not send message to user");
                }
                this.userImages.clearUserImages(fromId);
            });
        }, 5, TimeUnit.SECONDS);

        return BotUtil.buildAnswer(languageCode, MSG_GENERATING_PDF, callbackQueryId);
    }

    private BotApiMethod handlePhotoMessage(Message message) {
        final User from = message.getFrom();

        List<PhotoSize> images = message.getPhoto();
        String imageId = images.get(images.size() - 1).getFileId();

        final boolean added = userImages.add(from.getId(), imageId);
        if (!added) {
            return BotUtil.buildMessage(from, MSG_MAX_IMAGES, userImages.size(from.getId()))
                    .setReplyMarkup(KeyboardUtils.buildDeleteImagesKeyboard());
        }

        if (botActionsRateLimiter.action(from.getId())) {
            return BotUtil.buildMessage(from, MSG_IMAGE_ADDED)
                    .setReplyToMessageId(message.getMessageId())
                    .setReplyMarkup(KeyboardUtils.buildKeyboard(from.getLanguageCode()));
        }
        return null;
    }

    private BotApiMethod handleTextMessage(Message message, String text) {
        final User from = message.getFrom();

        if (text.startsWith("/")) {
            // Commands
            if (text.startsWith("/start")) {
                this.userImages.clearUserImages(from.getId());
                return BotUtil.buildMessage(from, MSG_START);
            }
        } else {
            // Non Commands
            if (message.isReply()) {
                Message reply = message.getReplyToMessage();

                log.info("User {} is renaming a document to {}", from.getId(), message.getText());

                Document document = reply.getDocument();
                if (document == null) {
                    return BotUtil.buildMessage(from, ERROR_MUST_REPLY_TO_DOCUMENT);
                }
                GetFile getFile = new GetFile()
                        .setFileId(document.getFileId());

                final String newFileName = message.getText() + ".pdf";

                try {
                    Paths.get(newFileName);
                } catch (InvalidPathException e) {
                    return BotUtil.buildMessage(from, ERROR_INVALID_FILE_NAME);
                }

                java.io.File file = null;
                try {
                    final File tgFile = execute(getFile);
                    file = downloadFile(tgFile);

                    InputMedia inputMedia = new InputMediaDocument();
                    inputMedia.setMedia(file, message.getText() + ".pdf");
                    inputMedia.setCaption(MSG_FILE_RENAME.formatted(from.getLanguageCode()));

                    EditMessageMedia editMessageMedia = new EditMessageMedia()
                            .setChatId(message.getChatId())
                            .setMessageId(reply.getMessageId());

                    editMessageMedia.setMedia(inputMedia);

                    try {
                        execute(editMessageMedia);
                    } catch (TelegramApiException ex) {
                        log.warn("Could not execute edit file, name {}", file.getName());
                        return BotUtil.buildMessage(from, ERROR_INVALID_FILE_NAME);
                    }

                    return BotUtil.buildMessage(from, MSG_FILE_RENAMED)
                            .setReplyToMessageId(reply.getMessageId());

                } catch (TelegramApiException e) {
                    log.error("Could not get file", e);
                } finally {
                    if (file != null && file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.error("Could not delete file {}", file.getName());
                        }
                    }
                }
            }

            final User forwardFrom = message.getForwardFrom();
            if (forwardFrom != null) {
                return new SendMessage()
                        .setChatId(message.getChatId())
                        .setText(forwardFrom.getFirstName() + "\n`" + forwardFrom.getId() + "`\nIs Member: " + isChannelMember(forwardFrom))
                        .enableMarkdown(true);
            }
        }
        return BotUtil.buildMessage(from, ERROR_SEND_START);
    }

    public boolean isChannelMember(User user) {
        if (channelMembersCache.containsKey(user.getId())) {
            return channelMembersCache.get(user.getId());
        }

        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId("@SwiperTeam");
        getChatMember.setUserId(user.getId());
        try {
            final ChatMember chatMember = execute(getChatMember);
            final String status = chatMember.getStatus();
            final boolean isMember = !(status.equalsIgnoreCase("left") | status.equalsIgnoreCase("kicked"));
            if (!isMember) {
                log.warn("A user tried to use the bot without joining the channel, status {}", status);
            }

            channelMembersCache.put(user.getId(), isMember);
            return isMember;
        } catch (TelegramApiException e) {
            log.warn(e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAdmin(int userId) {
        return Application.admins.contains(userId);
    }

}
