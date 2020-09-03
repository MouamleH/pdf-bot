package me.mouamle.bot.pdf.util;

import me.mouamle.bot.pdf.BotMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;

public class BotUtil {

    public static AnswerCallbackQuery buildAnswer(String language, BotMessage botMessage, String callbackQueryId) {
        return new AnswerCallbackQuery().setText(botMessage.formatted(language))
                .setCallbackQueryId(callbackQueryId)
                .setShowAlert(true);
    }

    public static SendMessage buildMessage(User user, BotMessage botMessage, Object... format) {
        return new SendMessage()
                .setChatId(String.valueOf(user.getId()))
                .setText(botMessage.formatted(user.getLanguageCode(), format));
    }


}
