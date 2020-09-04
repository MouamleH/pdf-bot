package me.mouamle.bot.pdf.util.keyboard;

import me.mouamle.bot.pdf.util.keyboard.image.BuildImageKeyboardAR;
import me.mouamle.bot.pdf.util.keyboard.image.BuildImageKeyboardEN;
import mouamle.generator.KeyboardGenerator;
import mouamle.generator.classes.ButtonHolder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyboardUtils {

    public static List<List<InlineKeyboardButton>> generateKeyboard(Object data) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        try {
            List<List<ButtonHolder>> buttons = KeyboardGenerator.getInstance().generateKeyboard(data);
            for (List<ButtonHolder> button : buttons) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                for (ButtonHolder holder : button) {
                    row.add(new InlineKeyboardButton(holder.getText()).setCallbackData(holder.getData()));
                }
                keyboard.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keyboard;
    }

    public static InlineKeyboardMarkup buildJoinKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(new InlineKeyboardButton("الإنضمام للقناة").setUrl("https://t.me/SwiperTeam")));
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup buildNewBotKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(new InlineKeyboardButton("الذهاب إلى البوت الجديد").setUrl("https://t.me/SwiperPDFBot?start=HI")));
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup buildKeyboard(String languageCode) {
        if ("ar".equals(languageCode)) {
            return new InlineKeyboardMarkup(generateKeyboard(new BuildImageKeyboardAR()));
        } else {
            return new InlineKeyboardMarkup(generateKeyboard(new BuildImageKeyboardEN()));
        }
    }

}
