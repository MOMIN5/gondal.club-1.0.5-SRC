// 
// Decompiled by Procyon v0.5.36
// 

package com.esoterik.client.util;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.nio.file.FileVisitOption;
import java.io.File;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.nio.file.Paths;

public class FileUtil
{
    public static boolean appendTextFile(final String data, final String file) {
        try {
            final Path path = Paths.get(file, new String[0]);
            Files.write(path, Collections.singletonList(data), StandardCharsets.UTF_8, Files.exists(path, new LinkOption[0]) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            System.out.println("WARNING: Unable to write file: " + file);
            return false;
        }
        return true;
    }
    
    public static List<String> readTextFileAllLines(final String file) {
        try {
            final Path path = Paths.get(file, new String[0]);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            System.out.println("WARNING: Unable to read file, creating new file: " + file);
            appendTextFile("", file);
            return Collections.emptyList();
        }
    }
    
    public static List<File> getFiles(final String dir) {
        try (final Stream<Path> paths = Files.walk(Paths.get(dir, new String[0]), new FileVisitOption[0])) {
            return (List<File>)paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map((Function<? super Path, ?>)Path::toFile).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList());
        }
        catch (Exception ex) {
            return new ArrayList<File>();
        }
    }
    
    public static Optional<File> getFile(final String name) {
        return Optional.of(new File(name));
    }
    
    public static String randomString() {
        final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        final StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; ++i) {
            final int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}
