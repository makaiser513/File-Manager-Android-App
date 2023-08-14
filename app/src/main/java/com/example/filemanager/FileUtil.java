package com.example.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    public static void copy(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyDirectory(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }

        String[] files = source.list();

        for (String file : files) {
            File srcFile = new File(source, file);
            File destFile = new File(destination, file);
            copy(srcFile, destFile);
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }
    public static void move(File source, File destination) throws IOException {
        copy(source, destination);
        deleteRecursively(source);
    }

    private static void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursively(child);
            }
        }

        fileOrDirectory.delete();
    }

}
