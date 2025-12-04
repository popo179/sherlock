package com.global_search.sherlock.service;

import com.global_search.sherlock.document.OrderDocument;
import com.global_search.sherlock.document.TripSearchDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.UpdateByQueryRequest;
import org.opensearch.client.opensearch.core.UpdateByQueryResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            String destination,
            String tripId
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

                    if (tripId != null && !tripId.isEmpty()) {
                        b.must(Query.of(q2 -> q2.match(m -> m
                                .field("tripId")
                                .query(v -> v.stringValue(tripId))
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

    public void updateShipment(String shipmentId, TripSearchDocument.Shipment shipment) {

        // Script to replace the matching order object
        String script =
                "for (int i = 0; i < ctx._source.shipments.size(); i++) {" +
                        "  if (ctx._source.shipments[i].shipmentId == params.shipmentId) {" +
                        "    ctx._source.shipments[i] = params.newShipment;" +
                        "  }" +
                        "}";

        UpdateByQueryRequest request = new UpdateByQueryRequest.Builder()
                .index("trip-index")
                .script(s -> s.inline(in -> {

                    in.lang("painless");
                    in.source(script);

                    // Prepare params
                    Map<String, JsonData> params = new HashMap<>();
                    params.put("shipmentId", JsonData.of(shipmentId));
                    params.put("newShipment", JsonData.of(shipment));

                    in.params(params);
                    return in;
                }))
                .build();

        try {
            UpdateByQueryResponse response = client.updateByQuery(request);
            System.out.println("Updated docs: " + response.updated());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Update failed", e);
        }
    }

    public void updateTripOrder(String orderId, OrderDocument updatedOrderDoc) {

        // Script to replace the matching order object
        String script =
                "for (int i = 0; i < ctx._source.order.size(); i++) {" +
                        "  if (ctx._source.order[i].orderId == params.orderId) {" +
                        "    ctx._source.order[i] = params.newOrder;" +
                        "  }" +
                        "}";

        UpdateByQueryRequest request = new UpdateByQueryRequest.Builder()
                .index("trip-index")
                .script(s -> s.inline(in -> {

                    in.lang("painless");
                    in.source(script);

                    // Prepare params
                    Map<String, JsonData> params = new HashMap<>();
                    params.put("orderId", JsonData.of(orderId));

                    // Map updatedOrderDoc to a JSON object for painless
                    Map<String, Object> newOrderMap = new HashMap<>();
                    newOrderMap.put("orderId", updatedOrderDoc.getOrderId());
                    newOrderMap.put("status", updatedOrderDoc.getStatus());
                    newOrderMap.put("customerName", updatedOrderDoc.getCustomerName());

                    params.put("newOrder", JsonData.of(newOrderMap));

                    in.params(params);
                    return in;
                }))
                .build();

        try {
            UpdateByQueryResponse response = client.updateByQuery(request);
            System.out.println("Updated docs: " + response.updated());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Update failed", e);
        }
    }

}
