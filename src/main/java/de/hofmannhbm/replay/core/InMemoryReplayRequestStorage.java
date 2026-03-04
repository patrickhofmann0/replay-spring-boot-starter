package de.hofmannhbm.replay.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryReplayRequestStorage implements ReplayRequestRepository {

    private final LinkedList<CapturedRequest> storage = new LinkedList<>();
    private final int maxSize;

    public InMemoryReplayRequestStorage(int maxSize) {
        this.maxSize = maxSize > 0 ? maxSize : 100;
    }

    @Override
    public synchronized void save(CapturedRequest request) {
        if (storage.size() >= maxSize) {
            storage.removeFirst(); // Ältesten Request löschen (FIFO)
        }
        storage.add(request);
    }

    @Override
    public synchronized List<CapturedRequest> findAll() {
        return new ArrayList<>(storage);
    }

    /**
     * für Testzwecke, um die gespeicherten Requests zu löschen.
     */
    public synchronized void clear() {
        storage.clear();
    }

    @Override
    public CapturedRequest findById(String id) {
        return storage.stream()
                      .filter(r -> r.id().equals(id))
                      .findFirst()
                      .orElse(null);
    }
}