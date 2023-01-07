package com.k8sjava.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Integer customerId;
    private List<Map<String, String>> customerData;
    private String message;
}
