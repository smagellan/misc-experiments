package smagellan.test.logcollector;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class RolledFileMessage extends GenericMessage<List<Path>> {
    public RolledFileMessage(List<Path> payload) {
        super(payload);
    }

    public RolledFileMessage(List<Path> payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    public RolledFileMessage(List<Path> payload, MessageHeaders headers) {
        super(payload, headers);
    }
}
