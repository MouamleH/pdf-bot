package me.mouamle.bot.pdf.web;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class ResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == 500) {
            log.error("server returned 500 with body {}, Converting to 200 for good luck.", responseContext.getEntity());
            responseContext.setStatus(200);
        }
    }

}
