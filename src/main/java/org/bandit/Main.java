package org.bandit;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class Main {

    public static void convertTextToPdf(String inputFile, String outputFile) {
        Document document = new Document();
        int maxRetries = 5;
        int retryDelayMs = 100;

        for(int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                PdfWriter.getInstance(document, new FileOutputStream(outputFile));
                document.open();
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    document.add(new Paragraph(line));
                }
                reader.close();
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    System.err.println("Error converting file to PDF after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    System.out.println("Error converting file to PDF, retrying in " + retryDelayMs + "ms");
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                e.printStackTrace();
            } finally {
                document.close();
            }
        }
    }

    public static void readFileWithRetry(String path) {
        int maxRetries = 5;
        int retryDelayMs = 100;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Reading file: " + path);
                Files.readAllLines(Paths.get(path)).forEach(System.out::println);
                return;
            } catch (IOException e) {
                if (attempt == maxRetries) {
                    System.err.println("Failed to read file after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    System.out.println("File access error, retrying in " + retryDelayMs + "ms");
                    try {
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    public static void main(String[] args)  {
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        Consumer<String> fileWatch = (path) -> {
            Path dir = Paths.get(path);
            WatchService watcher;
            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    System.out.print(Thread.currentThread().getName() + " ====> ");
                    System.out.println(kind.name() + ": " + filename);

                    // Handle file conversions and stuff later here
//                    readFileWithRetry(path + "/" + filename);
                    convertTextToPdf(path + "/" + filename, path + "/" + "convertedFile" + ".pdf");
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        };
        if (args.length == 0) {
            System.out.println("Please provide directories to watch");
            return;
        }
        /*
            Creates virtual threads that watches the given directories for file changes
         */
        Thread.Builder builder = Thread.ofVirtual()
                .name("Resource-Watcher", 1);
        List<Thread> threads = new ArrayList<>();

        for (String arg : args) {
            System.out.println("Watching directory: " + arg);
            Thread watchThread = builder.start(() -> fileWatch.accept(arg));
            threads.add(watchThread);
        }
        /*
            Graceful shutdown, add cleanup here if needed later
         */
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("Shutting down gracefully...");
                    for (Thread thread : threads) {
                        thread.interrupt();
                        System.out.println(
                                "Shutting down thread: " + thread.getName()
                        );
                    }
                    shutdownLatch.countDown();
                })
        );

        Thread.ofVirtual()
                .name("sigint‑watcher", 0)
                .start(() -> {
                    try {
                        shutdownLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

//        Thread.Builder builder = Thread.ofVirtual();
//        Thread fileWatchThread = builder.name(
//                "resources watcher"
//                )
//                .start(
//                () -> {
//                    System.out.println("Watching directory: src/main/resources");
//                    fileWatch.accept("src/main/resources");
//                }
//
//        );
//        Thread secondaryWatcherThread = builder.name(
//                "resources2 watcher"
//                )
//                .start(
//                () -> {
//                    System.out.println("Watching directory: src/main/resources2");
//                    fileWatch.accept("src/main/resources2");
//                }
//
//        );
//        fileWatchThread.join();
//        secondaryWatcherThread.join();
    }
}