package io.github.patrickhofmann0.replay.core;

import java.util.List;

public interface ReplayRequestRepository {
    void save(CapturedRequest request);

    List<CapturedRequest> findAll();

    CapturedRequest findById(String id);
}
