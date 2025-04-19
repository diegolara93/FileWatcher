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
                System.out.println("File converted to PDF successfully: " + outputFile);
                return;
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
        Consumer<ConverterImpl> fileWatch = (converter) -> {
            Path dir = Paths.get(converter.getInputFile());
            WatchService watcher;
            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                dir.register(watcher, ENTRY_CREATE);
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
//                    readFileWithRetry(converter + "/" + filename);
                    // Check file type extension of created file
                    String extension = filename.toString().substring(filename.toString().lastIndexOf(".") + 1);
                    switch (extension) {
                        case "txt" ->  converter = new TextConverter(converter.getInputFile(), converter.getOutputFile(), FileType.TXT);
                        case "docx" -> converter.setFileType(FileType.DOCX);
                        case "html" -> converter.setFileType(FileType.HTML);
                        case "json" -> converter.setFileType(FileType.JSON);
                        default -> System.out.println("Unsupported file type: " + extension);
                    }
                    converter.convertToPdf(converter.getInputFile() + "/" + filename, converter.getOutputFile() + "/" + filename  + ".pdf");
//                    convertTextToPdf(converter.getInputFile() + "/" + filename, converter.getInputFile() + "/output/" + filename  + ".pdf");
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        };
        if (args.length == 0) {
            System.out.println("Please provide directories to watch");
            System.out.println("Usage: <FileConversionType> <DirectoryToWatch> <DirectoryToWatch2>...");
            System.out.println("Example: pdf src/main/resources src/main/resources2 src/main/resources3");
            return;
        }
        /*
            Creates virtual threads that watches the given directories for file changes
         */
        Thread.Builder builder = Thread.ofVirtual()
                .name("Resource-Watcher", 1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            FileType fileType = null;
            if (i == 0) {
                if (!args[i].equals("pdf") &&
                        !args[i].equals("txt") &&
                        !args[i].equals("docx") &&
                        !args[i].equals("html") &&
                        !args[i].equals("json"))
                {
                    System.out.println("Invalid file conversion type: " + args[i]);
                    System.out.println("Supported types: pdf, txt, docx, html, json");
                    return;
                } else {
                    switch (args[i].toLowerCase()) {
                        case "pdf" -> fileType = FileType.PDF;
                        case "txt" -> fileType = FileType.TXT;
                        case "docx" -> fileType = FileType.DOCX;
                        case "html" -> fileType = FileType.HTML;
                        case "json" -> fileType = FileType.JSON;
                    }
                }
                System.out.println("Converting files to: " + fileType);
            } else {
                String arg = args[i];
                System.out.println("Watching directory: " + arg);
                ConverterImpl converterInfo = new ConverterImpl(arg, arg + "/output", fileType);
                Thread watchThread = builder.start(() -> fileWatch.accept(converterInfo));
                threads.add(watchThread);
            }
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