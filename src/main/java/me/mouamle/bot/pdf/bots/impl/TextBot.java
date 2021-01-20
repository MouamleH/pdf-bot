package me.mouamle.bot.pdf.bots.impl;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.bots.AbstractWebhookBot;
import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.messages.BotMessage;
import me.mouamle.bot.pdf.service.PDFTasks;
import me.mouamle.bot.pdf.service.UserDataService;
import me.mouamle.bot.pdf.util.BotUtil;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Queue;

import static me.mouamle.bot.pdf.messages.BotMessage.*;


@Slf4j
@SuppressWarnings("rawtypes")
public class TextBot extends AbstractWebhookBot {

    private final UserDataService<Integer, String> userTexts;

    public TextBot(BotData botData) {
        super(botData);
        userTexts = new UserDataService<>("user-texts", 100);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            final Message message = update.getMessage();
            if (message.hasText()) {
                final String text = message.getText();
                if (text.startsWith("/")) {
                    return handleCommands(message);
                }
                return handleText(message);
            }
        } else if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update.getCallbackQuery());
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
            return handleBuildPDF(callbackQueryId, from, languageCode, fromId);
        } else if (callbackQuery.getData().contains("clear-imgs")) {
            return handleClearText(callbackQueryId, languageCode, fromId);
        }
        return BotUtil.buildAnswer(languageCode, ERROR_GENERIC_ERROR, callbackQueryId);
    }

    private BotApiMethod handleClearText(String callbackQueryId, String languageCode, Integer fromId) {
        userTexts.clear(fromId);
        return BotUtil.buildAnswer(languageCode, TEXT_MSG_IMAGES_CLEARED, callbackQueryId);
    }

    private BotApiMethod handleBuildPDF(String callbackQueryId, User from, String languageCode, Integer fromId) {
        final Queue<String> texts = userTexts.get(fromId);
        PDFTasks.generateTextPDF(this, fromId, isAdmin(fromId), texts, file -> {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setDocument(file);
            sendDocument.setChatId(String.valueOf(fromId));
            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                log.error("Could not send document to user {}, msg: {}", fromId, e.getMessage());
            }

            boolean deleted = file.delete();
            if (!deleted) {
                log.error("Could not delete file {}", file.getName());
            }
            userTexts.clear(fromId);
        }, error -> {
            try {
                execute(BotUtil.buildMessage(from, error));
            } catch (TelegramApiException e) {
                log.error("Could not send message to user");
            }
            userTexts.clear(fromId);
        });
        return BotUtil.buildAnswer(languageCode, MSG_GENERATING_PDF, callbackQueryId);
    }

    private BotApiMethod handleText(Message message) {
        final User user = message.getFrom();
        if (!isChannelMember(user)) {
            return BotUtil.buildMessage(user, ERROR_MUST_JOIN)
                    .setReplyMarkup(KeyboardUtils.buildJoinKeyboard());
        }

        final boolean added = userTexts.add(user.getId(), message.getText());
        if (!added) {
            return BotUtil.buildMessage(user, TEXT_MSG_MAX_TEXTS, userTexts.size(user.getId()))
                    .setReplyMarkup(KeyboardUtils.buildDeleteImagesKeyboard());
        }

        if (botActionsRateLimiter.action(user.getId())) {
            return BotUtil.buildMessage(user, MSG_CONTENT_ADDED)
                    .setReplyToMessageId(message.getMessageId())
                    .setReplyMarkup(KeyboardUtils.buildKeyboard(user.getLanguageCode()));
        }

        return null;
    }

    private BotApiMethod handleCommands(Message message) {
        final String text = message.getText();
        if (text.startsWith("/start")) {
            return BotUtil.buildMessage(message.getFrom(), BotMessage.TEXT_MSG_START);
        }
        return null;
    }

}
