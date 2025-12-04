package com.global_search.sherlock.service;

import com.global_search.sherlock.document.OrderDocument;
import com.global_search.sherlock.document.TripSearchDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
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
                .id(trip.getTripCode())
                .document(trip)
        );
        IndexResponse response = client.index(request);
        System.out.println("Indexed trip: " + response.id());
    }

    public List<TripSearchDocument> searchTrips(
            String orderStatus,
            String shipmentCode,
            String origin,
            String destination,
            String tripCode,
            String orderCode,
            String consignmentCode,
            String tripStatus
    ) throws IOException {

        Query query = Query.of(q -> q
                .bool(b -> {

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

                    if (tripStatus != null && !tripStatus.isEmpty()) {
                        b.must(Query.of(q2 -> q2.match(m -> m
                                .field("status")
                                .query(v -> v.stringValue(tripStatus))
                        )));
                    }

                    // Shipment nested filter
                    if (shipmentCode != null && !shipmentCode.isEmpty()) {
                        b.must(Query.of(q2 -> q2.nested(n -> n
                                .path("shipments")
                                .query(Query.of(nq -> nq
                                        .term(t -> t
                                                .field("shipments.shipmentCode")
                                                .value(v -> v.stringValue(shipmentCode))
                                        )
                                ))
                        )));
                    }

                    if (tripCode != null && !tripCode.isEmpty()) {
                        b.must(Query.of(q2 -> q2.match(m -> m
                                .field("tripCode")
                                .query(v -> v.stringValue(tripCode))
                        )));
                    }

                    if (orderCode != null && !orderCode.isEmpty()) {
                        b.must(Query.of(q2 -> q2.nested(m -> m
                                .path("orders")
                                .query(Query.of(nq -> nq
                                    .term(t -> t
                                            .field("orders.orderCode")
                                            .value(v -> v.stringValue(orderCode))
                                    )
                                ))
                        )));
                    }

                    if (orderStatus != null && !orderStatus.isEmpty()) {
                        b.must(Query.of(q2 -> q2.nested(m -> m
                                .path("orders")
                                .query(Query.of(nq -> nq
                                        .term(t -> t
                                                .field("orders.status")
                                                .value(v -> v.stringValue(orderStatus))
                                        )
                                ))
                        )));
                    }

                    if (consignmentCode != null && !consignmentCode.isEmpty()) {
                        b.must(Query.of(q2 -> q2.nested(m -> m
                                .path("consignments")
                                .query(Query.of(nq -> nq
                                        .term(t -> t
                                                .field("consignments.consignmentCode")
                                                .value(v -> v.stringValue(consignmentCode))
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

    public void updateShipment(String shipmentCode, TripSearchDocument.Shipment shipment) {

        // Script to replace the matching order object
        String script =
                "for (int i = 0; i < ctx._source.shipments.size(); i++) {" +
                        "  if (ctx._source.shipments[i].shipmentCode == params.shipmentCode) {" +
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
                    params.put("shipmentCode", JsonData.of(shipmentCode));
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

    public void updateTripOrder(String orderCode, OrderDocument updatedOrderDoc) {

        // Script to replace the matching order object
        String script =
                "for (int i = 0; i < ctx._source.order.size(); i++) {" +
                        "  if (ctx._source.order[i].orderCode == params.orderCode) {" +
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
                    params.put("orderCode", JsonData.of(orderCode));

                    // Map updatedOrderDoc to a JSON object for painless
                    Map<String, Object> newOrderMap = new HashMap<>();
                    newOrderMap.put("orderCode", updatedOrderDoc.getOrderCode());
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
