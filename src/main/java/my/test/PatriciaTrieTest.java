package my.test;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.collections4.trie.analyzer.StringKeyAnalyzer;

/**
 * Created by vladimir on 7/29/16.
 */
public class PatriciaTrieTest {
    public static void main(String[] args) {
        PatriciaTrie<String> trie = new PatriciaTrie<>();
        trie.put("ab", "1");
        trie.put("1b", "2");
        System.err.println(trie.selectKey("11d"));


    }
}
