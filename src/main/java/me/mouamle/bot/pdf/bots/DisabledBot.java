package me.mouamle.bot.pdf.bots;

import me.mouamle.bot.pdf.Application;
import me.mouamle.bot.pdf.service.ConcurrentCache;
import me.mouamle.bot.pdf.service.RateLimiter;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;

@SuppressWarnings("rawtypes")
public class DisabledBot extends TelegramWebhookBot {

    private final Application.BotData botData;
    private final String responseMessage;
    private final RateLimiter<Long> botActionsRateLimiter;

    public DisabledBot(Application.BotData botData, String responseMessage) {
        this.botData = botData;
        this.responseMessage = responseMessage;

        ConcurrentCache<Long, Integer> botActionsCache = new ConcurrentCache<>(
                "bot-actions-cache",
                Duration.ofSeconds(4).toMillis(),
                Duration.ofSeconds(1).toMillis(),
                1024);
        botActionsRateLimiter = new RateLimiter<>("bot-actions", 1, false, botActionsCache);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            final Long chatId = update.getMessage().getChatId();
            if (botActionsRateLimiter.action(chatId)) {
                return new SendMessage(chatId, responseMessage).setReplyMarkup(KeyboardUtils.buildNewBotKeyboard());
            }
        }
        return null;
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
