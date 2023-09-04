package com.dddqmmx.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class FileUtil {
    public static File findLatestFileStartingWith(String directoryPath, String prefix) {
        File directory = new File(directoryPath);
        File[] matchingFiles = directory.listFiles((dir, name) -> name.startsWith(prefix));

        if (matchingFiles == null || matchingFiles.length == 0) {
            return null;
        }

        // Sort files by last modified timestamp in descending order
        Arrays.sort(matchingFiles, Comparator.comparingLong(File::lastModified).reversed());

        return matchingFiles[0];
    }
    public static String downloadFile(String fileUrl, String targetDirectory) throws IOException {
        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();

        String fileName = Paths.get(url.getPath()).getFileName().toString();
        Path targetPath = Paths.get(targetDirectory, fileName);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(targetPath.toFile())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return fileName;
        }
    }
}
