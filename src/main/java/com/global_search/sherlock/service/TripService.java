package com.global_search.sherlock.service;

import com.global_search.sherlock.document.TripSearchDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final OpenSearchClient client;

    public void indexTrip(TripSearchDocument trip) throws IOException {
        IndexRequest<TripSearchDocument> request = IndexRequest.of(i -> i
                .index("trip-index")
                .id(trip.getTripId())
                .document(trip)
        );
        IndexResponse response = client.index(request);
        System.out.println("Indexed trip: " + response.id());
    }

    public List<TripSearchDocument> searchTrips(
            String orderStatus,
            String shipmentOrderId,
            String origin,
            String destination
    ) throws IOException {

        Query query = Query.of(q -> q
                .bool(b -> {
                    // Order status filter
                    if (orderStatus != null && !orderStatus.isEmpty()) {
                        b.must(Query.of(q2 -> q2.term(t -> t
                                .field("order.status")
                                .value(v -> v.stringValue(orderStatus))
                        )));
                    }

                    if (origin != null && !origin.isEmpty()) {
                        b.must(Query.of(q2 -> q2.match(m -> m
                                .field("origin")
                                .query(v -> v.stringValue(origin))
                        )));
                    }

                    // Trip destination filter
                    if (destination != null && !destination.isEmpty()) {
                        b.must(Query.of(q2 -> q2.match(m -> m
                                .field("destination")
                                .query(v -> v.stringValue(destination))
                        )));
                    }

                    // Shipment nested filter
                    if (shipmentOrderId != null && !shipmentOrderId.isEmpty()) {
                        b.must(Query.of(q2 -> q2.nested(n -> n
                                .path("shipments")
                                .query(Query.of(nq -> nq
                                        .term(t -> t
                                                .field("shipments.externalCustomerOrderId")
                                                .value(v -> v.stringValue(shipmentOrderId))
                                        )
                                ))
                        )));
                    }

                    return b;
                })
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("trip-index")
                .query(query)
        );

        SearchResponse<TripSearchDocument> response = client.search(searchRequest, TripSearchDocument.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .toList();
    }
}
