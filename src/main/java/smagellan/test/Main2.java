package smagellan.test;

import org.apache.directory.mavibot.btree.BTree;
import org.apache.directory.mavibot.btree.BTreeFactory;
import org.apache.directory.mavibot.btree.InMemoryBTreeConfiguration;
import org.apache.directory.mavibot.btree.serializer.IntSerializer;
import org.apache.directory.mavibot.btree.serializer.StringSerializer;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by vladimir on 7/12/16.
 */
public class Main2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Main2.class);
    public static void main(String[] args) throws IOException {
        InMemoryBTreeConfiguration<Integer, String> conf = new InMemoryBTreeConfiguration<>();
        conf.setName("ff");
        conf.setKeySerializer(IntSerializer.INSTANCE );
        conf.setValueSerializer(StringSerializer.INSTANCE );
        BTree<Integer, String> tree = BTreeFactory.createInMemoryBTree( conf );
        tree.insert(1, "d");
        logger.info("finished");
    }
}
