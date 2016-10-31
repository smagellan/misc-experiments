package my.test.fs;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;


public class JimFsTest {
    public static void main(String[] args) throws IOException{
        final int kb = 1024;
        final long maxSize = 16 * kb;
        FileSystem fs = Jimfs.newFileSystem(
                Configuration
                .unix()
                .toBuilder()
                .setMaxSize(maxSize)
                .setBlockSize(4 * kb)
                .build());
        System.err.print("maxSize: " + maxSize + "\n");
        traceFs("initial", fs);
        Path foo = fs.getPath("/foo");
        Files.createDirectory(foo);
        traceFs("dir creation", fs);
        Path hello = foo.resolve("hello.txt"); // /foo/hello.txt
        Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);
        traceFs("file1 creation", fs);
        Path hello2 = foo.resolve("hello2.txt"); // /foo/hello2.txt
        Files.write(hello2, ImmutableList.of("hello world2"), StandardCharsets.UTF_8);
        traceFs("file2 creation", fs);
    }

    public static void traceFs(String descr, FileSystem fs) throws IOException {
        System.err.println("my.test.fs trace for " + descr);
        System.err.print("filestores:");
        for (FileStore store : fs.getFileStores()){
            System.err.println(store + "; unallocated: " + store.getUnallocatedSpace() + "; usable: " + store.getUsableSpace());
        }
        System.err.print("rootDirs:");
        for (Path rootDir : fs.getRootDirectories()){
            System.err.println(rootDir);
        }
    }
}
