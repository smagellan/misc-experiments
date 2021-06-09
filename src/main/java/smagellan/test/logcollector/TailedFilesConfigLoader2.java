package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.dsl.TailAdapterSpec;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Configuration
public class TailedFilesConfigLoader2 implements BeanDefinitionRegistryPostProcessor {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TailedFilesConfigLoader2.class);


    private final List<String> filesList;
    public static final String TAIL_FROM_FILE_START_OPTIONS = "-F -n 2147483647";

    public TailedFilesConfigLoader2() {
        this.filesList = Arrays.asList("/tmp/aa-test3", "/tmp/aa-test4");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String filename : filesList) {
            registerFileTailMessageProducer(filename, Constants.TAILED_AND_ROLLED_FILES_CHANNEL, true, registry);
        }
    }

    private void registerFileTailMessageProducer(String filename, String outputChannelName, boolean trackSinceBeginning,
                                                 BeanDefinitionRegistry registry) throws BeansException {
        String beanName = filename
                .replace('/', '_')
                .replace('\\', '_')
                .replace('-', '_') + "_message_producer";
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(IntegrationFlow.class, () -> convertingTailedFileFlow(new File(filename), trackSinceBeginning, outputChannelName))
                .setLazyInit(false);
        logger.info("registering bean {}", beanName);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
