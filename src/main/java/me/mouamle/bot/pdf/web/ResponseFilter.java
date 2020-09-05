package me.mouamle.bot.pdf.web;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Provider
public class ResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ENTITY_PROPERTY = "ENTITY_PROPERTY";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty(ENTITY_PROPERTY, getEntityBody(requestContext));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == 500) {
            log.error("server returned 500 with body {}, Converting to 200 for good luck.", responseContext.getEntity());
            log.error("CALL:\nREQUEST: {}\nResponse: {}", requestContext.getProperty(ENTITY_PROPERTY), responseContext.getEntity());
            responseContext.setStatus(200);
        }
    }

    private String getEntityBody(ContainerRequestContext requestContext) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = requestContext.getEntityStream();

        final StringBuilder b = new StringBuilder();
        try {
            ReaderWriter.writeTo(in, out);

            byte[] requestEntity = out.toByteArray();
            if (requestEntity.length == 0) {
                b.append("\n");
            } else {
                b.append(new String(requestEntity)).append("\n");
            }
            requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return b.toString();
    }
}
