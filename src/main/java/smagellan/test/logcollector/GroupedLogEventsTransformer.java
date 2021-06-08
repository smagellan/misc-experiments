package smagellan.test.logcollector;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class GroupedLogEventsTransformer implements GenericTransformer<Message<List<Message<?>>>, Message<GroupedLogEvents>> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GroupedLogEventsTransformer.class);

    @Override
    public Message<GroupedLogEvents> transform(Message<List<Message<?>>> source) {
        ListMultimap<Path, String> tailedLines = MultimapBuilder
                .linkedHashKeys()
                .arrayListValues()
                .build();
        List<Path> rolledFiles = new ArrayList<>();
        for (Message<?> msg : source.getPayload()) {
            Object payload = msg.getPayload();
            if (msg instanceof RolledFileMessage) {
                RolledFileMessage rollMsg = (RolledFileMessage) msg;
                rolledFiles.addAll(rollMsg.getPayload());
            } else if (payload instanceof String) {
                File file = msg.getHeaders().get(FileHeaders.ORIGINAL_FILE, File.class);
                if (file != null) {
                    tailedLines.put(file.toPath(), (String) payload);
                } else {
                    logger.error("did not find header '{}' in message for payload {}", FileHeaders.ORIGINAL_FILE, payload);
                }
            }
        }

        return new GenericMessage<>(new GroupedLogEvents(tailedLines, rolledFiles), source.getHeaders());
    }
}
