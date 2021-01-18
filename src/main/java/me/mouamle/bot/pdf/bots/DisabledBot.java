package me.mouamle.bot.pdf.bots;

import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.service.RateLimiter;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@SuppressWarnings("rawtypes")
public class DisabledBot extends AbstractWebhookBot {

    private final RateLimiter<Long> botActionsRateLimiter;

    public DisabledBot(BotData botData) {
        super(botData);
        botActionsRateLimiter = RateLimiter.builder()
                .name("bot-actions")
                .size(1024)
                .ttl(4)
                .build();
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            final Long chatId = update.getMessage().getChatId();
            if (botActionsRateLimiter.action(chatId)) {
                return new SendMessage(chatId, botData.getResponseMessage()).setReplyMarkup(KeyboardUtils.buildNewBotKeyboard("أضغط هنا للأنتقال الى القناة"));
            }
        }
        return null;
    }

}
