package com.global_search.sherlock.document;

import lombok.Data;

@Data
public class OrderDocument {
    private String orderCode;
    private String status;
    private String customerName;
}
