package com.global_search.sherlock.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEventDto {
    private String customerId;
    private String customerName;
    private String phoneNumber;
}

