package me.mouamle.bot.pdf.bots;

import me.mouamle.bot.pdf.loader.BotData;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

public abstract class AbstractWebhookBot extends TelegramWebhookBot {

    protected final BotData botData;

    protected AbstractWebhookBot(BotData botData) {
        this.botData = botData;
    }

    @Override
    public String getBotUsername() {
        return botData.getUsername();
    }

    @Override
    public String getBotToken() {
        return botData.getToken();
    }

    @Override
    public String getBotPath() {
        return getBotUsername();
    }

}
