package customer.controller;

import customer.domain.CustomerPointsRecord;
import customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/{customerId}/points")
    public ResponseEntity<Integer> getCustomerPointsAmount(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerPointsAmount(customerId));
    }

    @GetMapping("/{customerId}/points/history")
    public ResponseEntity<List<String>> getCustomerPointsHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerPointsHistory(customerId).stream()
                .map(CustomerPointsRecord::toString)
                .collect(Collectors.toList()));
    }
}
