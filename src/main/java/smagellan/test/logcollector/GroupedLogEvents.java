package smagellan.test.logcollector;

import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class GroupedLogEvents {
    private final ListMultimap<Path, Map<String, String>> tailedLines;
    private final List<Pair<LogFileInfo, Path>> rolledFiles;

    public GroupedLogEvents(ListMultimap<Path, Map<String, String>> tailedLines, List<Pair<LogFileInfo, Path>> rolledFiles) {
        this.tailedLines = tailedLines;
        this.rolledFiles = rolledFiles;
    }

    public ListMultimap<Path, Map<String, String>> tailedLines() {
        return tailedLines;
    }

    public List<Pair<LogFileInfo, Path>> rolledFiles() {
        return rolledFiles;
    }

    @Override
    public String toString() {
        return "GroupedLogEvents{" +
                "tailedLines=" + tailedLines +
                ", rolledFiles=" + rolledFiles +
                '}';
    }
}
