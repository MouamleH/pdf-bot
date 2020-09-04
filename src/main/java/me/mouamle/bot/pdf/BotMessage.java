package me.mouamle.bot.pdf;

public enum BotMessage {

    MSG_START(
            "أرسل لي صورة أو عدة صور لتحويلها إلى PDF\n" +
                    "أنضم الى قناتي @SwiperTeam لرؤية اخر التحديثات.\n" +
                    "ولمعرفة كيفية إستخدام البوت.\n",
            "أرسل لي صورة أو عدة صور لتحويلها إلى PDF\n" +
                    "أنضم الى قناتي @SwiperTeam لرؤية اخر التحديثات.\n" +
                    "ولمعرفة كيفية إستخدام البوت.\n"
    ),

    MSG_NO_IMAGES(
            "You don't have any images\nIf you had sent some in the past, try sending them again",
            "ليس لديك أي صور.\n" +
                    "إذا كنت قد أرسلت الصور سابقاً، حاول الإرسال مجدداً."
    ),
    MSG_IMAGE_ADDED(
            "Send more or press \"Generate PDF \uD83D\uDCC4\".\nWait 10 seconds before creating the document",
            "أرسل المزيد أو اضغط \"إنشاء المستند \uD83D\uDCC4\"." +
                    "\nأنتضر 10 ثواني قبل إنشاء المستند."
    ),
    MSG_IMAGES_CLEARED(
            "Removed all of your images.",
            "تمت إزالة كل الصور."
    ),

    MSG_MAX_IMAGES(
            "Can't have more than 32 images\nYou have %d/32",
            "لا يمكن إضافة أكثر من 32 صورة\n" +
                    "عدد صورك %d/32."
    ),

    MSG_FILE_RENAMED(
            "File renamed.",
            "تم تغيير إسم المستند."
    ),
    MSG_FILE_RENAME(
            "Reply to the file to change its name",
            "رد على المستند لتغيير الإسم."
    ),
    MSG_GENERATING_PDF(
            "Generating your pdf ⏳...\nPlease wait 10 to 20 seconds before pressing the button again.",
            "جارِ إنشاء المستند ⏳...\n" +
                    "الرجاء الأنتضار لمدة 10 الى 20 ثانية قبل الضغط على الزر مره ثانيه."
    ),

    ERROR_DOWNLOAD_ERROR(
            "Could not download your images, try again later.",
            "لم يتم تحميل الصورة، حاول في وقتٍ لاحق."
    ),
    ERROR_PDF_GENERATION_ERROR(
            "Could not generate the PDF file, contact bot admin",
            "لم يتم إنشاء المستند, تواصل مع مطَور البوت."
    ),
    ERROR_NO_SPAM(
            "you're doing too many actions, chill!\nAlso try again in 6 to 8 seconds",
            "كافي شبيك مرعوص، استچن!\n" + "لا تدوس الدكمه اكثر من مره.\n" + "ها وانتظر 6 الى 8 ثواني قبل لا تعيدها ❤️"
    ),
    ERROR_INVALID_FILE_NAME(
            "Invalid file name",
            "الإسم غير صالح."
    ),

    ERROR_GENERIC_ERROR(
            "An error occurred, Contact bot admin.",
            "حدثت مشكلة,  تواصل مع مطَور البوت."
    ),

    ERROR_MUST_JOIN(
            "Join our channel to see bot updates and to use the bot\n@SwiperTeam",
                "إنضم لقناتنا لمشاهدة اخبار وتحديثات البوت, ولإستخدام البوت\n" +
                        "بعد الأنضام عاود المحاولة."
    );

    private final String en, ar;

    public static String formatted(BotMessage botMessage, String language, Object... format) {
        return botMessage.formatted(language, format);
    }

    BotMessage(String en, String ar) {
        this.ar = ar;
        this.en = en;
    }

    public String formatted(String language, Object... format) {
        if ("ar".equals(language)) {
            return String.format(ar, format);
        }
        return String.format(en, format);
    }

    public String getEn() {
        return en;
    }

    public String getAr() {
        return ar;
    }

}
