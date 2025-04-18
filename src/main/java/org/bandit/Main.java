package org.bandit;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Consumer<String> fileWatch = (path) -> {
            Path dir = Paths.get(path);
            WatchService watcher;
            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
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

                    System.out.println(kind.name() + ": " + filename);

                    // Handle file conversions and stuff later here
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        };
        /*
            Creates virtual threads that watches the given directories for file changes
         */
        Thread.Builder builder = Thread.ofVirtual();
        Thread fileWatchThread = builder.start(
                () -> {
                    System.out.println("Watching directory: src/main/resources");
                    fileWatch.accept("src/main/resources");
                }

        );
        Thread secondaryWatcherThread = builder.start(
                () -> {
                    System.out.println("Watching directory: src/main/resources2");
                    fileWatch.accept("src/main/resources2");
                }

        );
        fileWatchThread.join();
        secondaryWatcherThread.join();
    }
}