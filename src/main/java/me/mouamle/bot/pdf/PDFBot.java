package me.mouamle.bot.pdf;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.service.ConcurrentCache;
import me.mouamle.bot.pdf.service.RateLimiter;
import me.mouamle.bot.pdf.service.UserDataService;
import me.mouamle.bot.pdf.util.BotUtil;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.mouamle.bot.pdf.BotMessage.*;

@Slf4j
@SuppressWarnings("rawtypes")
public class PDFBot extends TelegramWebhookBot {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

    private static final List<Integer> admins = Arrays.asList(
            121414901,
            1188784367
    );

    private final String token;
    private final String username;

    private final RateLimiter<Integer> buttonsRateLimiter;
    private final RateLimiter<Integer> botActionsRateLimiter;

    private final UserDataService<Integer, String> userImages;

    public PDFBot(Application.BotData botData) {
        this.token = botData.getToken();
        this.username = botData.getUsername();
        ConcurrentCache<Integer, Integer> buttonActionsCache = new ConcurrentCache<>(
                "user-actions-cache",
                Duration.ofSeconds(6).toMillis(),
                Duration.ofSeconds(2).toMillis(),
                1024 * 10);
        buttonsRateLimiter = new RateLimiter<>("buttons", 1, true, buttonActionsCache);

        ConcurrentCache<Integer, Integer> botActionsCache = new ConcurrentCache<>(
                "bot-actions-cache",
                Duration.ofSeconds(4).toMillis(),
                Duration.ofSeconds(1).toMillis(),
                1024);
        botActionsRateLimiter = new RateLimiter<>("bot-actions", 1, false, botActionsCache);
        userImages = new UserDataService<>("user-images", 32);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        final Message message = update.getMessage();
        if (update.hasMessage()) {
            if (message.hasText()) {
                return handleTextMessage(message, message.getText());
            } else if (message.hasPhoto()) {
                return handlePhotoMessage(message);
            }
        } else if (update.hasCallbackQuery()) {
            final CallbackQuery callbackQuery = update.getCallbackQuery();
            final String callbackQueryId = callbackQuery.getId();
            final User from = callbackQuery.getFrom();
            final String languageCode = from.getLanguageCode();

            if (!buttonsRateLimiter.action(from.getId())) {
                return BotUtil.buildAnswer(languageCode, ERROR_NO_SPAM, callbackQueryId);
            }

            if (callbackQuery.getData().contains("build-imgs")) {
                final Integer fromId = from.getId();

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
                        sendDocument.setCaption(MSG_FILE_RENAME.formatted(languageCode));

                        try {
                            execute(sendDocument);
                        } catch (TelegramApiException e) {
                            log.error("Could not send document to user {}, msg: {}", fromId, e.getMessage());
                        }

                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.error("Could not delete file {}", file.getName());
                        }
                    }, error -> {
                        try {
                            execute(BotUtil.buildMessage(from, error));
                        } catch (TelegramApiException e) {
                            log.error("Could not send message to user");
                        }
                    });
                }, 5, TimeUnit.SECONDS);

                return BotUtil.buildAnswer(languageCode, MSG_GENERATING_PDF, callbackQueryId);
            } else if (callbackQuery.getData().contains("clear-imgs")) {
                return BotUtil.buildAnswer(languageCode, MSG_IMAGES_CLEARED, callbackQueryId);

            }
            return BotUtil.buildAnswer(languageCode, ERROR_GENERIC_ERROR, callbackQueryId);
        }

        return null;
    }

    private BotApiMethod handlePhotoMessage(Message message) {
        final User from = message.getFrom();

        if (!isChannelMember(from)) {
            return BotUtil.buildMessage(from, ERROR_MUST_JOIN);
        }

        List<PhotoSize> photo = message.getPhoto();
        String imageId = photo.get(photo.size() - 1).getFileId();

        final boolean added = userImages.add(from.getId(), imageId);
        if (!added) {
            return BotUtil.buildMessage(from, MSG_MAX_IMAGES, userImages.size(from.getId()));
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

        if (!isChannelMember(from)) {
            return BotUtil.buildMessage(from, ERROR_MUST_JOIN);
        }

        if (text.startsWith("/")) {
            // Commands
            if (text.startsWith("/start")) {
                return BotUtil.buildMessage(from, MSG_START);
            }
        } else {
            // Non Commands
            if (message.isReply()) {
                Message reply = message.getReplyToMessage();

                log.info("User {} is renaming a document", from.getId());

                Document document = reply.getDocument();
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

                    execute(editMessageMedia);

                    return BotUtil.buildMessage(from, MSG_FILE_RENAMED)
                            .setReplyToMessageId(reply.getMessageId());

                } catch (TelegramApiException e) {
                    log.error("Could not get file", e);
                } finally {
                    if (file != null) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.error("Could not delete file {}", file.getName());
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isChannelMember(User user) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId("@SwiperTeam");
        getChatMember.setUserId(user.getId());
        try {
            final ChatMember chatMember = execute(getChatMember);
            final String status = chatMember.getStatus();
            return !(status.equalsIgnoreCase("left") | status.equalsIgnoreCase("kicked"));
        } catch (TelegramApiException e) {
            return false;
        }
    }

    private boolean isAdmin(int userId) {
        return admins.contains(userId);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotPath() {
        return getBotUsername();
    }


}
