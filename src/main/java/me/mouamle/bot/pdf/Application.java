package me.mouamle.bot.pdf;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class Application {

    private static final String defaultSettings = "{\n" +
            "  \"external_url\": \"\",\n" +
            "  \"internal_url\": \"\",\n" +
            "  \"bots\": [\n" +
            "    {\n" +
            "      \"username\": \"\",\n" +
            "      \"token\": \"\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static List<BotData> loadData(JSONObject root) {
        JSONArray bots = (JSONArray) root.get("bots");
        if (bots.isEmpty()) {
            log.error("At least one bot must be registered, register one in your settings.json");
            System.exit(-1);
        }

        return (List<BotData>) bots.stream()
                .map(o -> {
                    JSONObject botData = (JSONObject) o;
                    final String username = String.valueOf(botData.get("username")).trim();
                    final String token = String.valueOf(botData.get("token")).trim();
                    return new BotData(username, token);
                })
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws TelegramApiRequestException, IOException, ParseException {
        InputStream stream = Application.class.getClassLoader().getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);

        final Path tokensPath = Paths.get("settings.json");
        if (!Files.exists(tokensPath)) {
            log.error("could not find settings file, please fill in the generated one\n{}", defaultSettings);
            Files.write(tokensPath, defaultSettings.getBytes(), StandardOpenOption.CREATE);
            System.exit(-1);
        }

        JSONObject root = (JSONObject) new JSONParser().parse(Files.newBufferedReader(tokensPath));
        List<BotData> botsData = loadData(root);
        log.info("Registering {} bots", botsData.size());

        ApiContextInitializer.init();

        final DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setAllowedUpdates(Arrays.asList("message", "callback_query"));

        final String externalUrl = String.valueOf(root.get("external_url"));
        final String internalUrl = String.valueOf(root.get("internal_url"));
        TelegramBotsApi api = new TelegramBotsApi(externalUrl, internalUrl);

        for (BotData data : botsData) {
            log.info("Registering bot: {}", data.getUsername());
            api.registerBot(new PDFBot(data));
        }

        log.info("Bots registered successfully");
    }

    @Value
    public static class BotData {
        String username;
        String token;
    }

}

