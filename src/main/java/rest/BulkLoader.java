/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 *
 * @author bwarshaw
 */
public class BulkLoader {
    
    public static ArrayList<String> findAllFilesInTree(String filePath) {
        final ArrayList<String> filesFound = new ArrayList<String>();
        try {
            Path startPath = Paths.get(filePath);
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) {
                    //System.out.println("Dir: " + dir.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    filesFound.add(file.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesFound;
    }
}
