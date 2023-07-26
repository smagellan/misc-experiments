package smagellan.test;

import org.apache.directory.mavibot.btree.BTree;
import org.apache.directory.mavibot.btree.BTreeFactory;
import org.apache.directory.mavibot.btree.InMemoryBTreeConfiguration;
import org.apache.directory.mavibot.btree.serializer.IntSerializer;
import org.apache.directory.mavibot.btree.serializer.StringSerializer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by vladimir on 7/12/16.
 */
public class Main2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Main2.class);
    public static void main(String[] args) throws IOException {
        m2();
    }

    private static void m() throws IOException {
        InMemoryBTreeConfiguration<Integer, String> conf = new InMemoryBTreeConfiguration<>();
        conf.setName("ff");
        conf.setKeySerializer(IntSerializer.INSTANCE);
        conf.setValueSerializer(StringSerializer.INSTANCE);
        BTree<Integer, String> tree = BTreeFactory.createInMemoryBTree(conf);
        tree.insert(1, "d");
        logger.info("finished");
    }

    // Produces
    // java.nio.file.FileSystemException: /tmp: Too many open files.
    // Stream from Files.list(Paths.get("/tmp")) should be closed to fix the issue.
    public static void m2() throws IOException {
        long s = 0;
        for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
            Collection<Path> p = Files.list(Paths.get("/tmp"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            s += p.size();
        }
        System.err.println(s);
    }
}
