package com.k8sjava.customer.api;

import com.k8sjava.customer.dto.CustomerDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping
public class CustomerController {
    private static final Map<Integer, List<Map<String, String>>> CUSTOMERS = Map.of(
            1, List.of(Map.of("address", "UK"), Map.of("gender", "M")),
            2, List.of(Map.of("address", "US"), Map.of("gender", "F"))
    );
    private static final String ALL_DATA = "/api/v1/customer";
    private static final String CUSTOMERS_DATA = "/api/v1/customer/{customerId}/orders";

    @GetMapping(path = ALL_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getCustomerFullData() {
        return ResponseEntity
                .status(HttpStatus.FOUND.value())
                .body(CUSTOMERS.entrySet().stream()
                        .map(blob -> CustomerDto.builder()
                                .customerId(blob.getKey())
                                .customerData(blob.getValue())
                                .build())
                        .collect(Collectors.toList()));
    }

    @GetMapping(path = CUSTOMERS_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerDto> getCustomerData(@PathVariable @NotNull int customerId) {
        if (!CUSTOMERS.containsKey(customerId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND.value())
                    .body(CustomerDto.builder()
                            .message(String.format("No customer found for customerId = %s", customerId))
                            .build());
        }
        return ResponseEntity
                .status(HttpStatus.FOUND.value())
                .body(CustomerDto.builder()
                        .customerId(customerId)
                        .message("Customer found successfully")
                        .customerData(CUSTOMERS.get(customerId))
                        .build()
                );
    }
}
