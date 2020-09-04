package me.mouamle.bot.pdf.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import me.mouamle.bot.pdf.BotMessage;
import me.mouamle.bot.pdf.bots.PDFBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static me.mouamle.bot.pdf.BotMessage.ERROR_DOWNLOAD_ERROR;
import static me.mouamle.bot.pdf.BotMessage.ERROR_PDF_GENERATION_ERROR;

@Slf4j
public class PDFTasks {

    private static final Executor executor = Executors.newFixedThreadPool(8);
    private static final Executor adminsExecutor = Executors.newFixedThreadPool(16);

    public static void generatePDF(PDFBot bot, int userId, boolean isPaid,
                                   Collection<String> imageIds,
                                   Consumer<File> onSuccess, Consumer<BotMessage> onError) {
        final Executor current = isPaid ? adminsExecutor : executor;
        current.execute(() -> {
            log.info("Creating a document with {} images", imageIds.size());

            List<File> imagesFiles = new ArrayList<>();

            Iterator<String> iterator = imageIds.iterator();

            int i = 0;
            while (iterator.hasNext()) {
                String imageId = iterator.next();
                try {
                    final org.telegram.telegrambots.meta.api.objects.File tgFile = bot.execute(new GetFile().setFileId(imageId));
                    try {
                        java.io.File outputFile = new java.io.File(String.format("usr %d img %d.jpg", userId, i));
                        bot.downloadFile(tgFile, outputFile);
                        imagesFiles.add(outputFile);
                    } catch (TelegramApiException e) {
                        log.error("Could not download image file from user ({})", userId, e);
                    }
                } catch (TelegramApiException e) {
                    log.error("Could not execute get file for user ({})", userId, e);
                }

                i++;
            }

            if (imagesFiles.isEmpty()) {
                onError.accept(ERROR_DOWNLOAD_ERROR);
                return;
            }

            String outputFileName = String.format("%s.pdf", userId);
            try {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(outputFileName));
                document.open();

                document.addCreator("https://t.me/" + bot.getBotUsername());
                document.addAuthor("User: " + userId);

                for (java.io.File file : imagesFiles) {
                    Image image = Image.getInstance(file.getAbsolutePath());

                    image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                    float x = (PageSize.A4.getWidth() - image.getScaledWidth()) / 2;
                    float y = (PageSize.A4.getHeight() - image.getScaledHeight()) / 2;
                    image.setAbsolutePosition(x, y);

                    document.add(image);
                    document.newPage();
                }
                document.close();

                deleteFiles(imagesFiles);

                onSuccess.accept(new java.io.File(outputFileName));

            } catch (IOException | DocumentException e) {
                log.error("Could not create document", e);
                onError.accept(ERROR_PDF_GENERATION_ERROR);
                deleteFiles(imagesFiles);
                deleteFiles(Collections.singletonList(new java.io.File(outputFileName)));
            }
        });
    }

    private static void deleteFiles(List<java.io.File> files) {
        for (java.io.File imagesFile : files) {
            boolean deleted = imagesFile.delete();
            if (!deleted) {
                log.warn("Could not delete pdf {}", imagesFile.getName());
            }
        }
    }

}
