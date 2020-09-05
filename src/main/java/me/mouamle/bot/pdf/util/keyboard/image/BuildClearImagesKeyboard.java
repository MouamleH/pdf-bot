package me.mouamle.bot.pdf.util.keyboard.image;

import mouamle.generator.annotation.handlers.value.ButtonGroupValue;

public class BuildClearImagesKeyboard {

    @ButtonGroupValue(
            key = "kb-build",
            texts = {
                    "مسح الصور ❌"
            },
            callbacks = {
                    "clear-imgs"
            }
    )
    private Object meh;

}
