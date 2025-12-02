package com.global_search.sherlock.index;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenSearchIndexInitializer {

    private final OpenSearchClient client;

    @PostConstruct
    public void init() {
        try {
            CreateIndexResponse response = client.indices().create(c -> c
                    .index("trips")
                    .mappings(m -> m
                            .properties("tripId", p -> p.keyword(k -> k))
                            .properties("origin", p -> p.text(t -> t))
                            .properties("destination", p -> p.text(t -> t))
                            .properties("order", p -> p.object(o -> o
                                    .properties("orderId", f -> f.keyword(k -> k))
                                    .properties("status", f -> f.keyword(k -> k))
                                    .properties("customerName", f -> f.text(t -> t))
                            ))
                            .properties("shipments", p -> p.nested(n -> n
                                    .properties("shipmentId", f -> f.keyword(k -> k))
                                    .properties("externalCustomerOrderId", f -> f.keyword(k -> k))
                            ))
                    )
            );
            System.out.println("Trip index created: " + response.acknowledged());
        } catch (Exception e) {
            if (e.getMessage().contains("resource_already_exists_exception")) {
                System.out.println("Trip index already exists");
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
