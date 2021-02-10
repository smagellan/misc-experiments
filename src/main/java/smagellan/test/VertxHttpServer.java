package smagellan.test;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.impl.VertxFactory;
import io.vertx.core.json.JsonObject;
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
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(1);
                //.setInstances(Runtime.getRuntime().availableProcessors());
        vertx.deployVerticle(() -> new ServerVerticle(false),
                deploymentOptions, r -> logger.info("start succeeded: {}", r.succeeded()));
    }
}

class ServerVerticle extends AbstractVerticle {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ServerVerticle.class);
    private final boolean useHttps;

    ServerVerticle(boolean useHttps) {
        this.useHttps = useHttps;
    }


    @Override
    public void start(Promise<Void> startFuture) {
        HttpServerOptions httpOptions = new HttpServerOptions()
                .setInitialSettings(new Http2Settings())
                .setTcpFastOpen(true)
                .setTcpQuickAck(true)
                .setTcpCork(true);

        if (useHttps) {
            PemKeyCertOptions permKeyOptions = new PemKeyCertOptions()
                    .setKeyPath("tls/server-key.pem")
                    .setCertPath("tls/server-cert.pem");
            httpOptions = httpOptions
                    .setSsl(true)
                    .setUseAlpn(true)
                    .setSslEngineOptions(new OpenSSLEngineOptions())
                     //httpOptions.setSslEngineOptions(new JdkSSLEngineOptions());
                    .addEnabledCipherSuite("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
                    .setPemKeyCertOptions(permKeyOptions);
        }
        HttpServer server = getVertx().createHttpServer(httpOptions);

        server.requestHandler(ServerVerticle::rockerHttpHandler);
        server.listen(8080, ar -> {
            if (ar.succeeded()) {
                startFuture.complete(null);
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    public static void simpleHttpHandler(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.putHeader("Content-Type", "text/plain");
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

        engine.render(context, "TestRockerTemplate2.rocker.html", r -> afterRender(response, r));
    }

    public static void afterRender(HttpServerResponse response, AsyncResult<Buffer> result) {
        if (result.failed()) {
            logger.error("failed to render doc", result.cause());
            response.end();
            return;
        }
        response.end(result.result());
    }
}