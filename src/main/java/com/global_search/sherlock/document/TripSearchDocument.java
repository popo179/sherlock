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

    private String tripCode;
    private String origin;
    private String destination;
    private String status;
    private List<Shipment> shipments;
    private List<OrderDocument> orders;
    private List<Consignment>  consignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Shipment {
        private String shipmentCode;
        private String externalCustomerOrderId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Consignment {
        private String consignmentCode;
    }
}