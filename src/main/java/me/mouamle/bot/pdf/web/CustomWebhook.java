package me.mouamle.bot.pdf.web;

import com.google.inject.Inject;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.meta.generics.WebhookBot;
import org.telegram.telegrambots.updatesreceivers.RestApi;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CustomWebhook implements Webhook {

    private String keystoreServerFile;
    private String keystoreServerPwd;
    private String internalUrl;

    private final RestApi restApi;

    @Inject
    public CustomWebhook() {
        this.restApi = new RestApi();
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public void setKeyStore(String keyStore, String keyStorePassword) throws TelegramApiRequestException {
        this.keystoreServerFile = keyStore;
        this.keystoreServerPwd = keyStorePassword;
        validateServerKeystoreFile(keyStore);
    }

    public void registerWebhook(WebhookBot callback) {
        restApi.registerCallback(callback);
    }

    public void startServer() throws TelegramApiRequestException {
        ResourceConfig rc = new ResourceConfig();
        rc.register(restApi);
        rc.register(JacksonFeature.class);
        rc.register(ResponseFilter.class);
        rc.register(CustomExceptionMapper.class);

        final HttpServer grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc);

        try {
            grizzlyServer.start();
        } catch (IOException e) {
            throw new TelegramApiRequestException("Error starting webhook server", e);
        }
    }

    private URI getBaseURI() {
        return URI.create(internalUrl);
    }

    private static void validateServerKeystoreFile(String keyStore) throws TelegramApiRequestException {
        File file = new File(keyStore);
        if (!file.exists() || !file.canRead()) {
            throw new TelegramApiRequestException("Can't find or access server keystore file.");
        }
    }

}
