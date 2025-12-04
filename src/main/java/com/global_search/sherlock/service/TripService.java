package com.global_search.sherlock.service;

import com.global_search.sherlock.document.TripSearchDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final OpenSearchClient client;
    private final NLPService nlpService;

    public void indexTrip(TripSearchDocument trip) throws IOException {
        IndexRequest<TripSearchDocument> request = IndexRequest.of(i -> i
                .index("trip-index")
                .id(trip.getTripCode())
                .document(trip)
        );
        IndexResponse response = client.index(request);
        System.out.println("Indexed trip: " + response.id());
    }

    public void indexTrips(List<TripSearchDocument> trips) throws IOException {

        if (trips == null || trips.isEmpty()) {
            return;
        }

        List<BulkOperation> operations = trips.stream()
                .map(trip -> BulkOperation.of(op ->
                        op.index(idx -> idx
                                .index("trip-index")
                                .id(trip.getTripCode())
                                .document(trip)
                        )
                ))
                .toList();

        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));
        BulkResponse response = client.bulk(bulkRequest);

        if (response.errors()) {
            System.err.println("Bulk index had failures:");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    System.err.println("Error indexing id " + item.index() +
                            ": " + item.error().reason());
                }
            });
        } else {
            System.out.println("Bulk indexed " + trips.size() + " trips successfully");
        }
    }



    public List<TripSearchDocument> searchTrips(String naturalLanguage) throws IOException {

        // ⬅️ Call NLP service here
        Query query = nlpService.convertToDsl(naturalLanguage);

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("trip-index")
                .query(query)
        );

        SearchResponse<TripSearchDocument> response =
                client.search(searchRequest, TripSearchDocument.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }
}