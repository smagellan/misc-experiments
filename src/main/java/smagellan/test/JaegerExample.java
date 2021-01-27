package smagellan.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import io.opentracing.contrib.mongo.common.providers.PrefixSpanNameProvider;
import org.bson.Document;

//based on https://github.com/open-telemetry/opentelemetry-java/blob/master/examples/jaeger/
public class JaegerExample {
    // Jaeger Endpoint URL and PORT
    private final String ip; // = "jaeger";
    private final int port; // = 14250;

    private final OpenTelemetry otelSdk = createOtelSdk();

    // OTel API
    private final Tracer tracer =
            otelSdk.getTracer("io.opentelemetry.example.JaegerExample");

    public JaegerExample(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private OpenTelemetrySdk createOtelSdk() {
        return
                OpenTelemetrySdk.builder()
                        .setTracerProvider(
                                SdkTracerProvider.builder()
                                        .addSpanProcessor(SimpleSpanProcessor.create(zipkinExporter()))
                                        .build())
                        .buildAndRegisterGlobal();
    }

    private void setupExporter() {
        // Set to process the spans by the Jaeger Exporter
        //OpenTelemetrySdk.getGlobalTracerManagement()
        //        .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter()).build());
    }

    public SpanExporter zipkinExporter() {
        return ZipkinSpanExporter.builder()
                        .setEndpoint("http://localhost:9411/api/v2/spans")
                        .setServiceName("otel-zipkin-example")
                        .build();
    }

    public SpanExporter jaegerExporter() {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel =
                ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
        // Export traces to Jaeger
        return JaegerGrpcSpanExporter.builder()
                        .setServiceName("otel-jaeger-example")
                        .setChannel(jaegerChannel)
                        .setDeadlineMs(30000)
                        .build();
    }

    private void myWonderfulUseCase() {
        // Generate a span
        Span span = this.tracer.spanBuilder("Start my wonderful use case").startSpan();
        span.addEvent("Event 0");
        // execute my use case - here we simulate a wait
        doWork();
        span.addEvent("Event 1");
        span.end();
    }

    private void doWork() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // do the right thing here
        }
    }

    // graceful shutdown
    public void shutdown() {
        //openTelemetry.getTracerManagement().shutdown()
        OpenTelemetrySdk.getGlobalTracerManagement().shutdown();
    }

    public static void main(String[] args) {
        // Parsing the input
        if (args.length < 2) {
            System.out.println("Missing [hostname] [port]");
            System.exit(1);
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        // Start the example
        JaegerExample example = new JaegerExample(ip, port);
        example.setupExporter();
        // generate a few sample spans
        Span parentSpan = example.tracer.spanBuilder("parent_span").startSpan();
        try(Scope scope = parentSpan.makeCurrent()) {
            for (int i = 0; i < 10; i++) {
                example.myWonderfulUseCase();
            }
            doMongoWork();
            parentSpan.recordException(new RuntimeException("test exception"),
                    Attributes.of(SemanticAttributes.EXCEPTION_ESCAPED, false));
            parentSpan.setStatus(StatusCode.ERROR);
        } finally {
            parentSpan.end();
        }
        // Shutdown example
        example.shutdown();

        System.out.println("Bye");
    }

    private static void doMongoWork() {
        TracingCommandListener listener = new TracingCommandListener
                .Builder(OpenTracingShim.createTracerShim())
                .withSpanNameProvider(new PrefixSpanNameProvider("mongodb:"))
                .build();
        MongoClientOptions opts = new MongoClientOptions.Builder()
                .applicationName("testApp")
                .addCommandListener(listener)
                .build();
        try (MongoClient client = new MongoClient(new ServerAddress("localhost", 27017), opts)) {
            MongoDatabase db = client.getDatabase("mydb");
            MongoCollection<Document> col =  db.getCollection("mongojack-collection");
            col.estimatedDocumentCount();
            col.countDocuments();
            FindIterable<Document> docs = col.find();
            for (Document doc : docs) {
                System.err.println(doc);
            }
        }
    }
}
