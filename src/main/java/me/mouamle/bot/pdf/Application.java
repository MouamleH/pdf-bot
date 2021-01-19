package me.mouamle.bot.pdf;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.bots.DisabledBot;
import me.mouamle.bot.pdf.bots.OCRBot;
import me.mouamle.bot.pdf.bots.PDFBot;
import me.mouamle.bot.pdf.bots.Settings;
import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.loader.BotLoader;
import me.mouamle.bot.pdf.web.CustomWebhook;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.Webhook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;

@Slf4j
public class Application {

    public static final List<Integer> admins = Arrays.asList(
            121414901,
            1188784367
    );

    public static void main(String[] args) throws IOException, TelegramApiException {
        InputStream stream = Application.class.getClassLoader().getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);

        BotLoader.initSettings();
        final Settings settings = BotLoader.loadSettings();

        ApiContextInitializer.init();
        ApiContext.register(Webhook.class, CustomWebhook.class);

        final DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setAllowedUpdates(Arrays.asList("message", "callback_query"));
        TelegramBotsApi api = new TelegramBotsApi(settings.getExternalUrl(), settings.getInternalUrl());

        for (BotData data : settings.getBots()) {
            log.info("Registering {} as {}", data.getUsername(), data.getType());
            switch (data.getType()) {
                case DISABLED:
                    api.registerBot(new DisabledBot(data));
                    break;
                case IMAGE_TO_PDF:
                    api.registerBot(new PDFBot(data));
                    break;
                case OCR:
                    api.registerBot(new OCRBot(data));
                    break;
                case TEXT_TO_PDF:
                case MERGE_PDF:
                case EXTRACT_CONTENT:
                    log.error("Bot {} tried to register as {} but it's not implemented yet!", data.getUsername(), data.getType());
                    break;
            }
        }
    }

}

