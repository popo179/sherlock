package com.global_search.sherlock.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {
    private String orderId;
    private String customerId;
    private String originFacilityId;
    private Double orderValue;
    private String status;
}
