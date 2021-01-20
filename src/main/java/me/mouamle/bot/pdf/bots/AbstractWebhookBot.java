package me.mouamle.bot.pdf.bots;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.Application;
import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.service.ConcurrentCache;
import me.mouamle.bot.pdf.service.RateLimiter;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;

@Slf4j
public abstract class AbstractWebhookBot extends TelegramWebhookBot {

    protected final BotData botData;
    private final ConcurrentCache<Integer, Boolean> channelMembersCache;

    protected final RateLimiter<Integer> buttonsRateLimiter;
    protected final RateLimiter<Integer> botActionsRateLimiter;

    public AbstractWebhookBot(BotData botData) {
        this.botData = botData;

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

        this.channelMembersCache = new ConcurrentCache<>(
                "channel-members",
                Duration.ofSeconds(2).toMillis(),
                Duration.ofSeconds(1).toMillis(),
                1024
        );
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

    protected boolean isChannelMember(User user) {
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

    protected boolean isAdmin(int userId) {
        return Application.admins.contains(userId);
    }

}
