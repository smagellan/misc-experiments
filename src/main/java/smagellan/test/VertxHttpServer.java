package smagellan.test;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.impl.MyVertxFactory;
//import io.vertx.core.impl.VertxFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;
import org.slf4j.LoggerFactory;

public class VertxHttpServer {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VertxHttpServer.class);

    public static void main(String[] args) {
        System.err.println("ours pid: " + ProcessHandle.current().pid());
        VertxOptions options = new VertxOptions()
                .setPreferNativeTransport(true);
        Vertx vertx = new MyVertxFactory(options).vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(1);
                //.setInstances(Runtime.getRuntime().availableProcessors());
        vertx.deployVerticle(() -> new ServerVerticle(8080, false),
                deploymentOptions, r -> logger.info("server start succeeded: {}", r.succeeded()));

        //vertx.deployVerticle(() -> new ClientVerticle(8080), deploymentOptions,
        //        r -> logger.info("client start succeeded: {}", r.succeeded()));
    }
}

class ClientVerticle extends AbstractVerticle {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClientVerticle.class);

    private final int port;

    public ClientVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start(Promise<Void> startFuture) {
        startFuture.complete(null);
        getVertx().setTimer(5000, this::doHttpRequest);
    }

    private void doHttpRequest(Long timerId) {
        HttpClient client = getVertx().createHttpClient(new HttpClientOptions().setKeepAlive(true));
        client.request(HttpMethod.GET, port, "localhost", "http://localhost:" + port, r1 -> {
            logger.info("http succeeded: {}", r1.succeeded(), r1.cause());
            if (r1.succeeded()) {
                r1.result().send("", r2 -> {
                    logger.info("http response succeeded: {}", r2.succeeded(), r2.cause());
                    if (r2.succeeded()) {
                        logger.info("response: {}", r2.result().bodyHandler(body -> {
                            logger.info("body: {}", body);
                            client.close();
                        }));
                    }
                });
            }
        });
    }
}

class ServerVerticle extends AbstractVerticle {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ServerVerticle.class);
    private static final TemplateEngine engine = RockerTemplateEngine.create();

    private final boolean useHttps;
    private final int port;

    ServerVerticle(int port, boolean useHttps) {
        this.port = port;
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
        server.listen(port, ar -> {
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
        final JsonObject context = new JsonObject()
                .put("foo", "badger")
                .put("bar", "fox")
                .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"));

        HttpServerResponse response = request.response();
        response.putHeader("Content-Type", "text/plain");
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