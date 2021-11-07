package smagellan.test;


import com.google.common.base.Joiner;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;

public class ClassgraphTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClassgraphTest.class);
    private static final Joiner JOINER = Joiner.on("\n");

    public static void main(String[] args) {
        try (ScanResult scanResult =
                     new ClassGraph()
                             .verbose()                   // Log to stderr
                             .enableAllInfo()             // Scan classes, methods, fields, annotations
                             .acceptPackages("smagellan.test")      // Scan com.xyz and subpackages (omit to scan all packages)
                             .scan()) {                   // Start the scan
            try (ResourceList lst = scanResult.getAllResources()) {
                logger.info("duplicate paths: {}", lst.classFilesOnly().findDuplicatePaths());
                logger.info("duplicate path names: {}", JOINER.join(duplicateBaseNames(lst).asMap().entrySet()));
            }
        }
    }

    public static ListMultimap<String, Resource> duplicateBaseNames(ResourceList lst) {
        MultimapBuilder.ListMultimapBuilder<@Nullable Object, @Nullable Object> bldr = MultimapBuilder.hashKeys().arrayListValues();
        ListMultimap<String, Resource> tmp = bldr.build();
        for (var entry : lst.asMap().entrySet()) {
            String[] tokens = entry.getKey().split("/");
            String newKey = tokens.length > 0 ? tokens[tokens.length - 1] : entry.getKey();
            tmp.putAll(newKey, entry.getValue());
        }
        ListMultimap<String, Resource> result = bldr.build();
        for (var entry : tmp.asMap().entrySet()) {
            var values = entry.getValue();
            if (values.size() >= 2) {
                result.putAll(entry.getKey(), values);
            }
        }
        return result;
    }
}
