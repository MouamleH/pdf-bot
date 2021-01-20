package me.mouamle.bot.pdf.util;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.Application;
import me.mouamle.bot.pdf.bots.AbstractPollingBot;
import me.mouamle.bot.pdf.messages.BotMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

@Slf4j
public class BotUtil {

    private static AbstractPollingBot reportingBot;

    public static void setReportingBot(AbstractPollingBot reportingBot) {
        BotUtil.reportingBot = reportingBot;
    }

    public static AnswerCallbackQuery buildAnswer(String language, BotMessage botMessage, String callbackQueryId) {
        return (AnswerCallbackQuery) validate(new AnswerCallbackQuery().setText(botMessage.formatted(language))
                .setCallbackQueryId(callbackQueryId)
                .setShowAlert(true));
    }

    public static SendMessage buildMessage(User user, BotMessage botMessage, Object... format) {
        return (SendMessage) validate(new SendMessage()
                .setChatId(String.valueOf(user.getId()))
                .setText(botMessage.formatted(user.getLanguageCode(), format)));
    }

    @SuppressWarnings("rawtypes")
    private static BotApiMethod validate(BotApiMethod method) {
        try {
            method.validate();
        } catch (TelegramApiValidationException e) {
            log.error("Could not validate method {}, error: {}", method.getMethod(), e.getLocalizedMessage(), e);

            final StackTraceElement cause = e.getStackTrace()[3];
            if (reportingBot != null) {
                try {
                    String className = cause.getClassName();
                    className = className.substring(className.lastIndexOf(".") + 1);
                    String msg = String.format(
                            "Failed to validate `%s`\nReason: `%s`\n\nClass: *%s*\nMethod: *%s*\nLine Number: `%s`",
                            method.getMethod(), e.getLocalizedMessage(), className, cause.getMethodName(),
                            cause.getLineNumber()
                    );

                    reportingBot.execute(new SendMessage(String.valueOf(Application.admins.get(0)), msg).enableMarkdown(true));
                } catch (TelegramApiException telegramApiException) {
                    log.error("Could not send a report through reporting bot, reason {}", telegramApiException.getLocalizedMessage(), telegramApiException);
                }
            }
            log.error("Code tried to return an invalid response with method {}, {}", method.getMethod(), method);

            return null;
        }
        return method;
    }


}
