package me.mouamle.bot.pdf.util.keyboard.ocr;

import mouamle.generator.annotation.handlers.value.ButtonGroupValue;


public class BuildSelectLanguageKeyboard {

    @ButtonGroupValue(
            key = "kb-build",
            texts = {
                    "English",
                    "العربية"
            },
            callbacks = {
                    "en",
                    "ar"
            }
    )
    private Object kMeh;
}
