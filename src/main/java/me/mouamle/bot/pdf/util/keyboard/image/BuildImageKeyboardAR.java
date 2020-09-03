package me.mouamle.bot.pdf.util.keyboard.image;

import mouamle.generator.annotation.handlers.value.ButtonGroupValue;

public class BuildImageKeyboardAR {

    @ButtonGroupValue(
            key = "kb-build",
            texts = {
                    "إنشاء المستند \uD83D\uDCC4",
                    "مسح الصور ❌"
            },
            callbacks = {
                    "build-imgs",
                    "clear-imgs"
            }
    )
    private Object meh;

}
