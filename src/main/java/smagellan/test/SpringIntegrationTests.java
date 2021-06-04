package smagellan.test;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.integration.aggregator.FluxAggregatorMessageHandler;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

public class SpringIntegrationTests {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringIntegrationTests.class);

    //@Bean
    public IntegrationFlow tailTest2() {
        QueueChannel channel = new QueueChannel();
        AbstractEndpoint ep1 = FileTailTest.subscribe("/tmp/aa-test2", channel);
        return IntegrationFlows.from((MessageChannel) channel)
                .bridge(e -> e.poller(Pollers.fixedDelay(5000).maxMessagesPerPoll(3)))
                .handle((msg) -> System.err.println("tailTest2 msg:" + msg))
                .get();
    }

    //@Bean
    //@Autowired
    public Date windowingBridgeTest(IntegrationFlowContext integrationFlowContext) {
        IntegrationFlow fluxFlow =
                (flow) -> flow
                        .split(splitter(" "))
                        .channel(MessageChannels.flux())
                        .handle(fluxWindow(null, 10, Duration.ofSeconds(10)));

        IntegrationFlowContext.IntegrationFlowRegistration registration =
                integrationFlowContext.registration(fluxFlow).register();

        logger.info("fetching flux window:");
        @SuppressWarnings("unchecked")
        Flux<Message<?>> window =
                registration.getMessagingTemplate()
                        .convertSendAndReceive("0 1 2 3 4 5 6 7 8", Flux.class);
        logger.info("flux window: {}", window == null ? null : window.toStream().collect(Collectors.toList()));
        return new Date();
    }

    @NotNull
    private DefaultMessageSplitter splitter(String delim) {
        DefaultMessageSplitter ret = new DefaultMessageSplitter();
        ret.setDelimiters(delim);
        return ret;
    }



    @NotNull
    private FluxAggregatorMessageHandler fluxWindow(String correlationId, int windowSize, Duration windowDuration) {
        FluxAggregatorMessageHandler ret = new FluxAggregatorMessageHandler();
        ret.setWindowSize(windowSize);
        ret.setWindowTimespan(windowDuration);
        if (correlationId != null) {
            ret.setCorrelationStrategy((msg) -> msg.getHeaders().get(correlationId));
        }
        return ret;
    }
}
