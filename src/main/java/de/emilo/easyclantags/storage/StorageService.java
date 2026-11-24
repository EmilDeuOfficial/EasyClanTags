package de.emilo.easyclantags.storage;

import de.emilo.easyclantags.model.ClanData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class StorageService {

    private final Logger logger;
    private final Supplier<List<ClanData>> snapshot;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "EasyClanTags-Storage");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean pending = new AtomicBoolean(false);
    private final Storage backend;

    public StorageService(Storage backend, Supplier<List<ClanData>> snapshot, Logger logger) {
        this.backend = backend;
        this.snapshot = snapshot;
        this.logger = logger;
    }

    public List<ClanData> load() throws IOException {
        return backend.load();
    }

    public String describe() {
        return backend.describe();
    }

    public void requestSave() {
        if (executor.isShutdown()) {
            return;
        }
        if (pending.compareAndSet(false, true)) {
            executor.execute(() -> {
                pending.set(false);
                try {
                    backend.save(snapshot.get());
                } catch (IOException | RuntimeException e) {
                    logger.severe("Could not save clans: " + e.getMessage());
                }
            });
        }
    }

    public void shutdownWithoutSaving() {
        executor.shutdownNow();
    }

    public void shutdown() {
        if (executor.isShutdown()) {
            return;
        }
        try {
            executor.submit(() -> {
                try {
                    backend.save(snapshot.get());
                } catch (IOException | RuntimeException e) {
                    logger.severe("Could not save clans: " + e.getMessage());
                }
            });
        } catch (RuntimeException e) {
            logger.severe("Could not queue the final save: " + e.getMessage());
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                logger.warning("Storage thread did not finish within 15 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
