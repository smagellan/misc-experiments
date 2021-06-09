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
import org.springframework.integration.file.tail.OSDelegatingFileTailingMessageProducer;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Configuration
class TailedFilesConfigLoader implements BeanDefinitionRegistryPostProcessor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TailedFilesConfigLoader.class);

    private final List<String> filesList;
    public static final String TAIL_FROM_FILE_START_OPTIONS = "-F -n 2147483647";

    public TailedFilesConfigLoader() {
        this.filesList = Arrays.asList("/tmp/aa-test3", "/tmp/aa-test4");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String filename : filesList) {
            //TODO: register existing rolled non-imported files dirs beans, pass trackSinceBeginning if needed
            registerFileTailMessageProducer(filename, Constants.TAILED_AND_ROLLED_FILES_CHANNEL, true, registry);
        }
    }

    private void registerConvertingChannel(BeanDefinitionRegistry registry) {
        IntegrationFlows
                .from(Files.tailAdapter(new File("file")).nativeOptions("opts"))
                .transform(new GroupedLogEventsTransformer());
    }

    //TODO: transform lines via regex pattern (via channel + intermediate transformer for each tail lines producer)
    private void registerFileTailMessageProducer(String filename, String outputChannelName, boolean trackSinceBeginning,
                                                 BeanDefinitionRegistry registry) throws BeansException {
        String beanName = filename
                .replace('/', '_')
                .replace('\\', '_')
                .replace('-', '_') + "_message_producer";
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(OSDelegatingFileTailingMessageProducer.class, () -> fileTailer(new File(filename), trackSinceBeginning, outputChannelName))
                .setLazyInit(false);
        logger.info("registering bean {}", beanName);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private OSDelegatingFileTailingMessageProducer fileTailer(File file, boolean trackSinceBeginning, String outputChannelName) {
        OSDelegatingFileTailingMessageProducer ret = new OSDelegatingFileTailingMessageProducer();
        //TODO: maybe track for new rolled files on startup and use --lines=[+]Integer.MAX_VALUE tail option if new rolled files were detected
        ret.setFile(file);
        ret.setOutputChannelName(outputChannelName);
        if (trackSinceBeginning) {
            logger.warn("custom tail options '{}' for file {}", TAIL_FROM_FILE_START_OPTIONS, file);
            ret.setOptions(TAIL_FROM_FILE_START_OPTIONS);
        }
        return ret;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
