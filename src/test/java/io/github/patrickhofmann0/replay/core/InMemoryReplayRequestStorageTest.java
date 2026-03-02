package io.github.patrickhofmann0.replay.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InMemoryReplayRequestStorage.
 */
class InMemoryReplayRequestStorageTest {

    private InMemoryReplayRequestStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryReplayRequestStorage(5);
    }

    @Test
    void shouldSaveAndRetrieveRequest() {
        // Given
        CapturedRequest request = createRequest("id-1", "GET", "/api/test");

        // When
        storage.save(request);

        // Then
        List<CapturedRequest> all = storage.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0)).isEqualTo(request);
    }

    @Test
    void shouldFindRequestById() {
        // Given
        CapturedRequest request1 = createRequest("id-1", "GET", "/api/test1");
        CapturedRequest request2 = createRequest("id-2", "POST", "/api/test2");
        storage.save(request1);
        storage.save(request2);

        // When
        CapturedRequest found = storage.findById("id-2");

        // Then
        assertThat(found).isEqualTo(request2);
    }

    @Test
    void shouldReturnNullForNonExistentId() {
        // Given
        storage.save(createRequest("id-1", "GET", "/api/test"));

        // When
        CapturedRequest found = storage.findById("non-existent");

        // Then
        assertThat(found).isNull();
    }

    @Test
    void shouldReturnNullWhenStorageIsEmpty() {
        // When
        CapturedRequest found = storage.findById("any-id");

        // Then
        assertThat(found).isNull();
    }

    @Test
    void shouldReturnEmptyListWhenNoRequestsStored() {
        // When
        List<CapturedRequest> all = storage.findAll();

        // Then
        assertThat(all).isEmpty();
    }

    @Test
    void shouldEnforceFifoOrderWhenMaxSizeExceeded() {
        // Given: Storage with max size of 3
        storage = new InMemoryReplayRequestStorage(3);
        CapturedRequest req1 = createRequest("id-1", "GET", "/api/1");
        CapturedRequest req2 = createRequest("id-2", "GET", "/api/2");
        CapturedRequest req3 = createRequest("id-3", "GET", "/api/3");
        CapturedRequest req4 = createRequest("id-4", "GET", "/api/4");

        // When: Save 4 requests (exceeds max size of 3)
        storage.save(req1);
        storage.save(req2);
        storage.save(req3);
        storage.save(req4);

        // Then: First request should be removed (FIFO)
        List<CapturedRequest> all = storage.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).containsExactly(req2, req3, req4);
        assertThat(all).doesNotContain(req1);
    }

    @Test
    void shouldContinuouslyRemoveOldestWhenExceedingLimit() {
        // Given: Storage with max size of 2
        storage = new InMemoryReplayRequestStorage(2);

        // When: Save 5 requests
        for (int i = 1; i <= 5; i++) {
            storage.save(createRequest("id-" + i, "GET", "/api/" + i));
        }

        // Then: Only last 2 requests should remain
        List<CapturedRequest> all = storage.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).id()).isEqualTo("id-4");
        assertThat(all.get(1).id()).isEqualTo("id-5");
    }

    @Test
    void shouldClearAllRequests() {
        // Given
        storage.save(createRequest("id-1", "GET", "/api/1"));
        storage.save(createRequest("id-2", "POST", "/api/2"));
        assertThat(storage.findAll()).hasSize(2);

        // When
        storage.clear();

        // Then
        assertThat(storage.findAll()).isEmpty();
    }

    @Test
    void shouldUseDefaultMaxSizeWhenZeroProvided() {
        // Given: Storage with max size of 0 (should use default 100)
        storage = new InMemoryReplayRequestStorage(0);

        // When: Save 101 requests
        for (int i = 1; i <= 101; i++) {
            storage.save(createRequest("id-" + i, "GET", "/api/" + i));
        }

        // Then: Should have 100 requests (default max size)
        List<CapturedRequest> all = storage.findAll();
        assertThat(all).hasSize(100);
        assertThat(all.get(0).id()).isEqualTo("id-2"); // First one removed
        assertThat(all.get(99).id()).isEqualTo("id-101"); // Last one present
    }

    @Test
    void shouldUseDefaultMaxSizeWhenNegativeProvided() {
        // Given: Storage with negative max size (should use default 100)
        storage = new InMemoryReplayRequestStorage(-10);

        // When: Save 101 requests
        for (int i = 1; i <= 101; i++) {
            storage.save(createRequest("id-" + i, "GET", "/api/" + i));
        }

        // Then: Should have 100 requests (default max size)
        assertThat(storage.findAll()).hasSize(100);
    }

    @Test
    void shouldBeThreadSafeForSave() throws InterruptedException {
        // Given
        storage = new InMemoryReplayRequestStorage(1000);
        int threadCount = 10;
        int requestsPerThread = 10;
        Thread[] threads = new Thread[threadCount];

        // When: Multiple threads save requests concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String id = "thread-" + threadId + "-req-" + j;
                    storage.save(createRequest(id, "GET", "/api/test"));
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then: All requests should be saved
        assertThat(storage.findAll()).hasSize(threadCount * requestsPerThread);
    }

    @Test
    void shouldReturnCopyOfStorageNotOriginal() {
        // Given
        CapturedRequest request = createRequest("id-1", "GET", "/api/test");
        storage.save(request);

        // When: Get all requests and try to modify the list
        List<CapturedRequest> firstCall = storage.findAll();
        storage.save(createRequest("id-2", "POST", "/api/test2"));
        List<CapturedRequest> secondCall = storage.findAll();

        // Then: Lists should be independent
        assertThat(firstCall).hasSize(1);
        assertThat(secondCall).hasSize(2);
    }

    private CapturedRequest createRequest(String id, String method, String path) {
        return new CapturedRequest(
                id,
                method,
                path,
                null,
                Map.of(),
                "",
                200,
                "",
                LocalDateTime.now()
        );
    }
}

