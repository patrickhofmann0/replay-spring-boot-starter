package de.hofmannhbm.replay.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryReplayRequestStorage implements ReplayRequestRepository {

    private final ArrayDeque<CapturedRequest> storage = new ArrayDeque<>();
    private final Map<String, CapturedRequest> idIndex = new HashMap<>();
    private final int maxSize;

    public InMemoryReplayRequestStorage(int maxSize) {
        this.maxSize = maxSize > 0 ? maxSize : 100;
    }

    @Override
    public synchronized void save(CapturedRequest request) {
        if (storage.size() >= maxSize) {
            CapturedRequest removed = storage.removeFirst(); // Ältesten Request löschen (FIFO)
            idIndex.remove(removed.id());
        }
        storage.add(request);
        idIndex.put(request.id(), request);
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
        idIndex.clear();
    }

    @Override
    public synchronized CapturedRequest findById(String id) {
        return idIndex.get(id);
    }
}