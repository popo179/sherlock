package com.global_search.sherlock.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchDocument {

    private String tripId;
    private String origin;
    private String destination;
    private List<Shipment> shipments;
    private List<OrderDocument> order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Shipment {
        private String shipmentId;
        private String externalCustomerOrderId;
    }
}
