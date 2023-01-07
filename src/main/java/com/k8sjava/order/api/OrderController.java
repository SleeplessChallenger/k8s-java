package com.k8sjava.order.api;

import com.k8sjava.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
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

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class OrderController {
    private static final String CUSTOMER_ORDER = "/api/v1/order/customer/{customerId}";
    private static final Map<Integer, List<String>> ORDERS = Map.of(
            1, List.of("Apples", "Bananas"), 2, List.of("Strawberry", "Peaches"));

    @GetMapping(path = CUSTOMER_ORDER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> getCustomerOrder(@PathVariable @NotNull int customerId) {
        if (!ORDERS.containsKey(customerId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND.value())
                    .body(OrderDto.builder()
                            .message(String.format("No message found for customerId = %s", customerId))
                            .build());
        }
        return ResponseEntity
                .status(HttpStatus.FOUND.value())
                .body(OrderDto.builder()
                        .customerId(customerId)
                        .orders(ORDERS.get(customerId))
                        .message("Customer found successfully")
                        .build());
    }
}
