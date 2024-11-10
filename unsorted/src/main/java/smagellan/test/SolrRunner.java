package smagellan.test;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

import java.io.IOException;

public class SolrRunner {
    public static void main(String[] args) throws IOException {
        CoreContainer container = new CoreContainer();
        //SolrCore core = new SolrCore();
        try (EmbeddedSolrServer server = new EmbeddedSolrServer(container, "core1")) {
            SolrInputDocument doc = new SolrInputDocument();
        }
    }
}
