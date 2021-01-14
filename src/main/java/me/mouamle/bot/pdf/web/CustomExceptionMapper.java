package me.mouamle.bot.pdf.web;

import me.mouamle.bot.pdf.Application;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CustomExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        exception.printStackTrace(pw);

        final String stackTrace = sw.toString();
        System.err.println(stackTrace);

        pw.close();

        return Response.ok(new SendMessage(String.valueOf(Application.admins.get(0)), stackTrace)).build();
    }

}
