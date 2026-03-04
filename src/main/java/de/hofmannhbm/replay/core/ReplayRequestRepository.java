package de.hofmannhbm.replay.core;

import java.util.List;

public interface ReplayRequestRepository {
    void save(CapturedRequest request);

    List<CapturedRequest> findAll();

    CapturedRequest findById(String id);
}
