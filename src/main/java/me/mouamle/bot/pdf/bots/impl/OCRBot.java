package me.mouamle.bot.pdf.bots.impl;

import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.bots.AbstractWebhookBot;
import me.mouamle.bot.pdf.loader.BotData;
import me.mouamle.bot.pdf.util.BotUtil;
import me.mouamle.bot.pdf.util.keyboard.KeyboardUtils;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import static me.mouamle.bot.pdf.messages.BotMessage.*;

@Slf4j
public class OCRBot extends AbstractWebhookBot {
    private File file = null;

    public OCRBot(BotData botData) {
        super(botData);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        final Message message = update.getMessage();

        if (update.hasMessage()) {
            this.handleDocumentMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            this.handleCallbackQuery(update.getCallbackQuery());
        } else {

            SendMessage sendMessage = BotUtil.buildMessage(message.getFrom(), MESSAGE_MUST_BE_DOCUMENT);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String language = callbackQuery.getData().substring("ButtonValue;kMeh;kb-build;".length());
        Message message = callbackQuery.getMessage();

        if (file != null && file.exists()) {
            Tesseract tesseract = new Tesseract();

            try {
                execute(new SendMessage().setText("Please wait...").setChatId(message.getChatId()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            try {
                URL ocrData = getClass().getClassLoader().getResource("ocrData");
                tesseract.setDatapath(Objects.requireNonNull(ocrData).toString().substring(6));
                tesseract.setLanguage(language);
                String text = tesseract.doOCR(file);
                execute(new SendMessage().setText(text).setChatId(message.getChatId()));
            } catch (TesseractException | TelegramApiException e) {
                e.printStackTrace();
            } finally {
                if (file.delete()) {
                    log.info("File Deleted");
                } else {
                    log.info("file was not deleted");
                }
            }
        }
    }

    private void handleDocumentMessage(Message message) {
        if (message.hasDocument()) {

            SendMessage sendMessage = BotUtil.buildMessage(message.getFrom(), OCR_CHOOSE_LANGUAGE)
                    .setReplyMarkup(KeyboardUtils.buildSelectLanguageKeyboardOCR(message.getFrom().getLanguageCode()));

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            Document document = message.getDocument();
            GetFile getFile = new GetFile().setFileId(document.getFileId());

            try {
                final String tgFile = execute(getFile).getFilePath();
                file = downloadFile(tgFile, new File("assets/" + document.getFileUniqueId() + "." + document.getMimeType().substring("image/".length())));
            } catch (TelegramApiException e) {
                log.error("Could not get file", e);
                e.printStackTrace();
            }
        } else {
            SendMessage sendMessage = BotUtil.buildMessage(message.getFrom(), MESSAGE_MUST_BE_DOCUMENT);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public void getLanguageKeyboardLayout(Message message) {
//        KeyboardUtils.generateKeyboard();
    }
}
