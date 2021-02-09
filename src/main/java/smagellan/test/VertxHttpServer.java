package smagellan.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.impl.VertxFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;
import org.slf4j.LoggerFactory;

public class VertxHttpServer {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VertxHttpServer.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions()
                .setPreferNativeTransport(true);
        Vertx vertx = new VertxFactory(options).vertx();
        HttpServerOptions httpOptions = new HttpServerOptions()
                .setInitialSettings(new Http2Settings())
                .setTcpFastOpen(true)
                .setTcpQuickAck(true)
                .setTcpCork(true);

        httpOptions.setSsl(true);
        httpOptions.setUseAlpn(true);
        httpOptions.setSslEngineOptions(new OpenSSLEngineOptions());
        //httpOptions.setSslEngineOptions(new JdkSSLEngineOptions());
        httpOptions.addEnabledCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        httpOptions.setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem"));

        HttpServer server = vertx.createHttpServer(httpOptions);

        server.requestHandler(VertxHttpServer::rockerHttpHandler);
        server.listen(8080);
    }

    public static void simpleHttpHanlder(HttpServerRequest request) {
        // This handler gets called for each request that arrives on the server
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "text/plain");

        // Write to the response and end it
        response.end("Hello World!");
    }

    public static void rockerHttpHandler(HttpServerRequest request) {
        TemplateEngine engine = RockerTemplateEngine.create();
        final JsonObject context = new JsonObject()
                .put("foo", "badger")
                .put("bar", "fox")
                .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"));

        HttpServerResponse response = request.response();
        response.putHeader("content-type", "text/plain");

        engine.render(context, "TestRockerTemplate2.rocker.html", r -> renderHandler(response, r));
    }

    public static void renderHandler(HttpServerResponse response, AsyncResult<Buffer> result) {
        if (result.failed()) {
            logger.error("failed to render doc", result.cause());
            response.end();
            return;
        }
        response.end(result.result());
    }
}