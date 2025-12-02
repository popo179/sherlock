package com.global_search.sherlock.repository;

import com.global_search.sherlock.document.TripSearchDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripSearchRepository {

    private final OpenSearchClient client;

    public TripSearchDocument save(TripSearchDocument doc) {
        try {
            IndexRequest<TripSearchDocument> request = IndexRequest.of(i -> i
                    .index("trip-index")
                    .id(doc.getTripId())
                    .document(doc)
            );

            IndexResponse response = client.index(request);

            // ensure returned doc contains ID from OpenSearch
            doc.setTripId(response.id());

            return doc;

        } catch (Exception e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }
}
