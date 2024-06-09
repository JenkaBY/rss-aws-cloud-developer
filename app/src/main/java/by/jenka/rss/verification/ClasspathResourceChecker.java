package by.jenka.rss.verification;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class ClasspathResourceChecker {

    public static final String RESOURCES_STATIC_WEBAPP = "./build/resources/main/static";

    public void run() {
        try {
            listFilesUsingFileWalkAndVisitor(RESOURCES_STATIC_WEBAPP).forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Set<String> listFilesUsingFileWalkAndVisitor(String dir) throws IOException {
        Set<String> fileList = new HashSet<>();
        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!Files.isDirectory(file)) {
                    fileList.add(file.toFile().getAbsolutePath().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }
}
