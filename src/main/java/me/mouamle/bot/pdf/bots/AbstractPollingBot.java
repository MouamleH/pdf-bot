package me.mouamle.bot.pdf.bots;

import me.mouamle.bot.pdf.loader.BotData;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class AbstractPollingBot extends TelegramLongPollingBot {

    private final BotData botData;

    public AbstractPollingBot(BotData botData) {
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
