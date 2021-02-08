package smagellan.test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.MyVertxFactory;
import io.vertx.core.impl.VertxFactory;

public class VertxHttpServer {
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions()
                .setPreferNativeTransport(true);
        Vertx vertx = new VertxFactory(options).vertx();
        HttpServerOptions httpOptions = new HttpServerOptions().setTcpFastOpen(true).setTcpQuickAck(true);
        HttpServer server = vertx.createHttpServer(httpOptions);

        server.requestHandler(VertxHttpServer::httpHanlder);
        server.listen(8080);
    }

    public static void httpHanlder(HttpServerRequest request) {
        // This handler gets called for each request that arrives on the server
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "text/plain");

        // Write to the response and end it
        response.end("Hello World!");
    }
}