package smagellan.test.logcollector;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class RolledFileMessage extends GenericMessage<Collection<Path>> {
    public RolledFileMessage(Collection<Path> payload) {
        super(payload);
    }

    public RolledFileMessage(Collection<Path> payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    public RolledFileMessage(Collection<Path> payload, MessageHeaders headers) {
        super(payload, headers);
    }
}
