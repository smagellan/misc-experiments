package smagellan.test.logcollector;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class DirectoryWatcher extends MessageProducerSupport implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final Collection<File> dirs2Watch;
    private final FilenameFilter filter;

    private Thread watcherThread;
    private WatchService svc;
    private List<WatchKey> dirsWatchKeys;

    //TODO: maybe switch to org.springframework.integration.file.filters.FileListFilter
    DirectoryWatcher(Collection<File> dirs2Watch, FilenameFilter filter) {
        this.dirs2Watch = dirs2Watch;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            WatchKey key;
            while ((key = svc.take()) != null) {
                logger.info("key: {}", key);
                MessageChannel channel = getOutputChannel();
                List<WatchEvent<?>> events = key.pollEvents();
                if (channel == null) {
                    logger.info("output channel is null, events are discarded");
                } else {
                    List<Path> contexts = events
                            .stream()
                            .map(WatchEvent::context)
                            .map(obj -> (Path) obj)
                            .filter(path -> filter.accept(path.toAbsolutePath().getParent().toFile(), path.getFileName().toString()))
                            .collect(Collectors.toList());
                    logger.info("publishing files events for {}", contexts);
                    channel.send(new RolledFileMessage(contexts));
                }
                key.reset();
            }
        } catch (InterruptedException ex) {
            logger.info("directory watcher thread stopping(interrupted)");
        }
    }

    @Override
    protected void doStart() {
        logger.info("starting directory watcher at {}", dirs2Watch);
        try {
            initWatcher();
            watcherThread = new Thread(this);
            watcherThread.start();
        } catch (IOException ex) {
            try {
                teardownWatcher();
            } catch (IOException ex1) {
                RuntimeException tmp = new RuntimeException(ex);
                tmp.addSuppressed(ex1);
                throw tmp;
            }
            throw new RuntimeException(ex);
        }
    }

    private void initWatcher() throws IOException {
        svc = FileSystems.getDefault().newWatchService();
        dirsWatchKeys = new ArrayList<>(dirs2Watch.size());
        for (File dir : dirs2Watch) {
            if (dir.exists()) {
                Path path = dir.toPath();
                //TODO: check which event log4j2 generates: StandardWatchEventKinds.ENTRY_MODIFY or ENTRY_CREATE (or both)
                WatchKey dirWatchKey = path.register(svc,
                        new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE},
                        SensitivityWatchEventModifier.MEDIUM);
                dirsWatchKeys.add(dirWatchKey);
            } else {
                logger.info("won't watch non-existent directory: {}", dir);
            }
        }
    }

    @Override
    protected void doStop() {
        try {
            watcherThread.interrupt();
            logger.info("waiting for directory watcher thread");
            watcherThread.join();
            logger.info("waiting for directory watcher thread done");
            teardownWatcher();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void teardownWatcher() throws IOException {
        if (dirsWatchKeys != null) {
            for (WatchKey key : dirsWatchKeys) {
                key.cancel();
            }
            dirsWatchKeys = null;
        }
        if (svc != null) {
            svc.close();
            svc = null;
        }
    }
}
