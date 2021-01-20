package me.mouamle.bot.pdf.messages;

public enum BotMessage {

    /* Generic */
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
    ERROR_MUST_REPLY_TO_DOCUMENT(
            "Reply to a document to change its name",
            "رد على مستند لتغير اسمه"
    ),
    ERROR_SEND_START(
            "send /start",
            "أَرسِل /start"
    ),

    ERROR_GENERIC_ERROR(
            "An error occurred, Contact bot admin.",
            "حدثت مشكلة,  تواصل مع مطَور البوت."
    ),

    ERROR_MUST_JOIN(
            "Join our channel to see bot updates and to use the bot\n@SwiperTeam",
            "إنضم للقناة حتى تستطيع استخدام البوت"
    ),
    /* Generic */

    /* PDF BOT */
    PDF_MSG_START(
            "أرسل لي صورة أو عدة صور لتحويلها إلى PDF\n" +
                    "أنضم الى قناتي @SwiperTeam لرؤية اخر التحديثات.\n" +
                    "ولمعرفة كيفية إستخدام البوت.\n",
            "أرسل لي صورة أو عدة صور لتحويلها إلى PDF\n" +
                    "أنضم الى قناتي @SwiperTeam لرؤية اخر التحديثات.\n" +
                    "ولمعرفة كيفية إستخدام البوت.\n"
    ),

    PDF_MSG_NO_IMAGES(
            "You don't have any images\nIf you had sent some in the past, try sending them again",
            "ليس لديك أي صور.\n" +
                    "إذا كنت قد أرسلت الصور سابقاً، حاول الإرسال مجدداً."
    ),
    MSG_CONTENT_ADDED(
            "Send more or press \"Generate PDF \uD83D\uDCC4\".\nWait 10 seconds before creating the document",
            "أرسل المزيد أو اضغط \"إنشاء المستند \uD83D\uDCC4\"." +
                    "\nأنتضر 10 ثواني قبل إنشاء المستند."
    ),
    PDF_MSG_IMAGES_CLEARED(
            "Removed all of your images.",
            "تمت إزالة كل الصور."
    ),

    PDF_MSG_MAX_IMAGES(
            "Can't have more than 32 images\nYou have %d/32",
            "لا يمكن إضافة أكثر من 32 صورة\n" +
                    "عدد صورك %d/32."
    ),

    PDF_MSG_N_IMAGES(
            "File has %d images.",
            "المستند يحتوي على %d صورة."
    ),

    PDF_ERROR_DOWNLOAD_ERROR(
            "Could not download your images, try again later.",
            "لم يتم تحميل الصورة، حاول في وقتٍ لاحق."
    ),
    /* PDF BOT */

    /* Text Bot */
    TEXT_MSG_START(
            "Send a text message or multiple to put in a PDF\n" +
                    "Join our channel @SwiperTeam to see all updates\n",
            "أرسل لي كتابة في رسالة او في عدة رسائل لتحويلها الى PDF\n" +
                    "أنضم الى قناتي @SwiperTeam لرؤية اخر التحديثات.\n" +
                    "ولمعرفة كيفية إستخدام البوت."),
    TEXT_MSG_MAX_TEXTS(
            "Can't have more than 32 messages\nYou have %d/32",
            "لا يمكن إضافة أكثر من 32 رسالة\n" +
                    "عدد رسائلك %d/32."
    ),
    TEXT_MSG_IMAGES_CLEARED(
            "Removed all of your texts.",
            "تمت إزالة كل الرسائل."
    ),
    /* Text Bot */;

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
