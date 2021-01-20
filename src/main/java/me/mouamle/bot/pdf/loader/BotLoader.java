package me.mouamle.bot.pdf.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

@Slf4j
public class BotLoader {

    private static final int VERSION = 2;
    private static final Path SETTINGS_PATH = Paths.get("settings.json");

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public static void initSettings() throws IOException {
        if (Files.exists(SETTINGS_PATH)) {
            final Settings settings = GSON.fromJson(Files.newBufferedReader(SETTINGS_PATH), Settings.class);
            if (VERSION != settings.getVersion()) {
                log.error("Settings version mismatch");
                System.exit(-1);
            }
        } else {
            final Settings settings = new Settings();
            settings.setVersion(VERSION);
            settings.setBots(Collections.singletonList(new BotData()));
            final String json = PRETTY_GSON.toJson(settings);
            log.error("could not find settings file, please fill in the generated one\n{}", json);
            Files.write(SETTINGS_PATH, json.getBytes(), StandardOpenOption.CREATE);
            System.exit(-1);
        }
    }

    public static Settings loadSettings() throws IOException {
        return GSON.fromJson(Files.newBufferedReader(SETTINGS_PATH), Settings.class);
    }

}
