package smagellan.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import io.opentracing.contrib.mongo.common.providers.PrefixSpanNameProvider;
import org.bson.Document;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

//based on https://github.com/open-telemetry/opentelemetry-java/blob/master/examples/jaeger/
public class OpenTelemetryExample {
    private static void myWonderfulUseCase(io.opentelemetry.api.trace.Tracer tracer) {
        Span span = tracer.spanBuilder("/otel-endpoint").startSpan();
        try {
            span.addEvent("Event 0");
            span.setAttribute("client_state", "starting: " + UUID.randomUUID());
            // execute my use case - here we simulate a wait
            doWork();
            span.addEvent("Event 1");
        } finally {
            span.end();
        }
    }

    private static void doWork() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // do the right thing here
        }
    }

    private void doMongoWork(MongoClient client) {
        MongoDatabase db = client.getDatabase("mydb");
        MongoCollection<Document> col =  db.getCollection("mongojack-collection");
        col.estimatedDocumentCount();
        col.countDocuments();
        FindIterable<Document> docs = col.find();
        for (Document doc : docs) {
            System.err.println(doc);
        }
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(OpenTelemetryConfiguration.class);
            ctx.registerShutdownHook();
            ctx.refresh();

            io.opentelemetry.api.trace.Tracer tracer = ctx.getBean(io.opentelemetry.api.trace.Tracer.class);
            Span parentSpan = tracer.spanBuilder("parent_span").startSpan();

            try (Scope scope = parentSpan.makeCurrent()) {
                for (int i = 0; i < 10; i++) {
                    myWonderfulUseCase(tracer);
                }
                //example.doMongoWork();
                parentSpan.recordException(new RuntimeException("test exception"),
                        Attributes.of(SemanticAttributes.EXCEPTION_ESCAPED, false));
                parentSpan.setStatus(StatusCode.ERROR);
            } finally {
                parentSpan.end();
            }
            System.out.println("Bye");
        }
    }
}


@Configuration
class OpenTelemetryConfiguration {
    // Jaeger Endpoint URL and PORT
    private static final String JAEGER_HOST = "localhost";
    private static final int JAEGER_PORT = 14250;

    @Bean
    public SdkTracerProvider sdkTracerProvider(SpanExporter exporter) {
        Resource serviceNameResource =
                Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, "otel-example",
                        ResourceAttributes.HOST_ARCH, ResourceAttributes.HostArchValues.AMD64
                        //ResourceAttributes.HOST_NAME, "unknown-hostname"
                ));
        return SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();
    }

    @Bean
    public SpanExporter zipkinExporter() {
        return ZipkinSpanExporter.builder()
                .setEndpoint("http://localhost:9411/api/v2/spans")
                .build();
    }

    @Bean
    public OpenTelemetrySdk createOtelSdk(SdkTracerProvider tracerProvider) {
        return OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .buildAndRegisterGlobal();
    }

    @Bean
    public io.opentracing.Tracer tracerShim(io.opentelemetry.api.trace.Tracer tracer) {
        return OpenTracingShim.createTracerShim(tracer);
    }

    @Bean
    public io.opentelemetry.api.trace.Tracer tracer(OpenTelemetrySdk otelSdk) {
        return otelSdk.getTracer("io.opentelemetry.example.JaegerExample");
    }

    @Lazy
    @Bean
    public MongoClient mongoClient(io.opentracing.Tracer tracerShim) {
        TracingCommandListener listener = new TracingCommandListener
                .Builder(tracerShim)
                .withSpanNameProvider(new PrefixSpanNameProvider("mongodb:"))
                .build();
        MongoClientOptions opts = new MongoClientOptions.Builder()
                .applicationName("testApp")
                .addCommandListener(listener)
                .build();

        return new MongoClient(new ServerAddress("localhost", 27017), opts);
    }

    public SpanExporter jaegerExporter() {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel =
                ManagedChannelBuilder.forAddress(JAEGER_HOST, JAEGER_PORT).usePlaintext().build();
        // Export traces to Jaeger
        return JaegerGrpcSpanExporter.builder()
                .setChannel(jaegerChannel)
                .setTimeout(30000, TimeUnit.MILLISECONDS)
                .build();
    }

    private void setupExporter() {
        // Set to process the spans by the Jaeger Exporter
        //OpenTelemetrySdk.getGlobalTracerManagement()
        //        .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter()).build());
    }
}