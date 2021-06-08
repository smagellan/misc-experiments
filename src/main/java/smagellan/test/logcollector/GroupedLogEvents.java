package smagellan.test.logcollector;

import com.google.common.collect.ListMultimap;

import java.nio.file.Path;
import java.util.List;

class GroupedLogEvents {
    private final ListMultimap<Path, String> tailedLines;
    private final List<Path> rolledFiles;

    public GroupedLogEvents(ListMultimap<Path, String> tailedLines, List<Path> rolledFiles) {
        this.tailedLines = tailedLines;
        this.rolledFiles = rolledFiles;
    }

    public ListMultimap<Path, String> tailedLines() {
        return tailedLines;
    }

    public List<Path> rolledFiles() {
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
