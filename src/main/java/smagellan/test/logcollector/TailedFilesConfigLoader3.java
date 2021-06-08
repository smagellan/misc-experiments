package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.dsl.TailAdapterSpec;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@EnableIntegration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TailedFilesConfigLoader3 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TailedFilesConfigLoader2.class);


    private final List<String> filesList;
    private final List<String> rolledFilesDirs;
    private final List<String> tailFlowsIds;
    private final IntegrationFlowContext ctx;
    public static final String TAIL_FROM_FILE_START_OPTIONS = "-F -n 2147483647";

    @Autowired
    public TailedFilesConfigLoader3(@Value("${key}") String key, IntegrationFlowContext ctx) {
        this.ctx = ctx;
        this.filesList = Arrays.asList("/tmp/aa-test3", "/tmp/aa-test4");
        this.rolledFilesDirs = Arrays.asList("/tmp/mon-dir1", "/tmp/mon-dir2");
        this.tailFlowsIds = Collections.unmodifiableList(buildFileTailerIds());
    }

    @Bean
    @Qualifier(Constants.CONTROL_BUS)
    public IntegrationFlow controlBus() {
        return IntegrationFlowDefinition::controlBus;
    }

    private List<String> buildFileTailerIds() {
        return IntStream.range(0, filesList.size())
                .boxed()
                .map(idx -> "fileTailer" + idx)
                .collect(Collectors.toList());
    }

    @Bean
    @Qualifier(Constants.FILE_TAILER_IDS)
    public List<String> fileTailerIds() {
        return tailFlowsIds;
    }

    @PostConstruct
    void registerTailedLogsFlows() {
        logger.info("registering log tailers");
        Iterator<String> tailerIds = tailFlowsIds.iterator();
        for (String filename : filesList) {
            ctx.registration(convertingTailedFileFlow(new File(filename), true, Constants.TAILED_AND_ROLLED_FILES_CHANNEL))
                    .id(tailerIds.next())
                    .autoStartup(false)
                    .register();
        }
    }

    public IntegrationFlow convertingTailedFileFlow(File file, boolean trackSinceBeginning, String outputChannelName) {
        TailAdapterSpec spec = Files.tailAdapter(file);
        if (trackSinceBeginning) {
            spec = spec.nativeOptions(TAIL_FROM_FILE_START_OPTIONS);
        }
        return IntegrationFlows
                .from(spec)
                .transform(new LogLineTransformer())
                .channel(outputChannelName)
                .get();
    }
}
