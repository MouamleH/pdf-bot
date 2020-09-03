package me.mouamle.bot.pdf.util.keyboard.image;

import mouamle.generator.annotation.handlers.value.ButtonGroupValue;

public class BuildImageKeyboardEN {

    @ButtonGroupValue(
            key = "kb-build",
            texts = {
                    "Generate PDF \uD83D\uDCC4",
                    "Clear Images ❌"
            },
            callbacks = {
                    "build-imgs",
                    "clear-imgs"
            }
    )
    private Object meh;

}
