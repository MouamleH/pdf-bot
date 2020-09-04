package me.mouamle.bot.pdf.bots;

import me.mouamle.bot.pdf.Application;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class AbstractBot extends TelegramLongPollingBot {

    private final Application.BotData botData;

    public AbstractBot(Application.BotData botData) {
        this.botData = botData;
    }

    @Override
    public void onUpdateReceived(Update update) { }

    @Override
    public String getBotUsername() {
        return botData.getUsername();
    }

    @Override
    public String getBotToken() {
        return botData.getToken();
    }

}
