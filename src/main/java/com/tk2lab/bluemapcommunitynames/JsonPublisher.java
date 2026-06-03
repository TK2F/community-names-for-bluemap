package com.tk2lab.bluemapcommunitynames;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class JsonPublisher {
    private final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    private final ExecutorService executor;
    private final Logger logger;
    private final AtomicBoolean atomicFallbackWarned = new AtomicBoolean(false);
    private volatile Instant lastSuccessfulWrite;
    private volatile String lastErrorSummary = "none";

    public JsonPublisher(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.executor = Executors.newSingleThreadExecutor(new WriterThreadFactory());
    }

    public void publishAsync(PlayersDocument document, Path target) {
        executor.execute(() -> {
            try {
                publishNow(document, target);
            } catch (IOException | RuntimeException ex) {
                lastErrorSummary = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                logger.warning("Failed to write BlueMapCommunityNames JSON: " + lastErrorSummary);
            }
        });
    }

    public void publishNow(PlayersDocument document, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                gson.toJson(document, writer);
                writer.write('\n');
            }
            moveIntoPlace(temp, target);
            lastSuccessfulWrite = Instant.now();
            lastErrorSummary = "none";
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private void moveIntoPlace(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            if (atomicFallbackWarned.compareAndSet(false, true)) {
                logger.warning("Atomic move is not supported for players.json; falling back to replace-existing moves.");
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Instant lastSuccessfulWrite() {
        return lastSuccessfulWrite;
    }

    public String lastErrorSummary() {
        return lastErrorSummary;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private static final class WriterThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "BlueMapCommunityNames-json-writer");
            thread.setDaemon(true);
            return thread;
        }
    }
}
