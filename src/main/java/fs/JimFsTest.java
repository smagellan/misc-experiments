package fs;
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
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setMaxSize(1024 * 8).build());
        traceFs(fs);
        Path foo = fs.getPath("/foo");
        Files.createDirectory(foo);

        Path hello = foo.resolve("hello.txt"); // /foo/hello.txt
        Files.write(hello, ImmutableList.of("hello world"), StandardCharsets.UTF_8);
    }

    public static void traceFs(FileSystem fs) throws IOException {
        System.err.print("filestores:");
        for (FileStore store : fs.getFileStores()){
            System.err.println(store + "; unallocated: " + store.getUnallocatedSpace());
        }
        System.err.print("rootDirs:");
        for (Path rootDir : fs.getRootDirectories()){
            System.err.println(rootDir);
        }
    }
}
