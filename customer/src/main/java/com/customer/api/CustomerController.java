package com.customer.api;

import com.customer.dto.CustomerDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
    private static final ObjectWriter MAPPER = new ObjectMapper().writer().withDefaultPrettyPrinter();
    @Value("${order.msa_host}")
    private String host;
    @Value("${order.msa_port}")
    private String port;

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

    @GetMapping(path = "/api/v1/order/data/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCustomerOrderData(@PathVariable @NotNull int customerId) throws JsonProcessingException {
        final String fullUrl = "http://" + host + ":" + port + "/api/v1/order/customer/" + customerId;
        final Object response = new RestTemplate().getForObject(fullUrl, Object.class);
        return ResponseEntity
                .status(HttpStatus.FOUND.value())
                .body(MAPPER.writeValueAsString(response));
    }

    @ExceptionHandler(value = {HttpClientErrorException.class})
    public ResponseEntity<String> catchErrors(Exception ex) throws JsonProcessingException {
        final String message = ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST.value())
                .body(MAPPER.writeValueAsString(message));
    }
}
