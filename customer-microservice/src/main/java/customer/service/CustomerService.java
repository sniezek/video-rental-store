package customer.service;

import customer.repository.CustomerRepository;
import customer.domain.CustomerPointsRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public int getCustomerPointsAmount(Long customerId) {
        return customerRepository.getCustomerPointsRecords(customerId).stream()
                .mapToInt(CustomerPointsRecord::getPoints)
                .sum();
    }

    public List<CustomerPointsRecord> getCustomerPointsHistory(Long customerId) {
        return customerRepository.getCustomerPointsRecordsSortedFromEarliest(customerId);
    }
}
