package customer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import customer.domain.CustomerPointsRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;
    private final CustomerPointsRecordMapper recordMapper;

    public CustomerRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.recordMapper = new CustomerPointsRecordMapper(objectMapper);
    }

    public List<CustomerPointsRecord> getCustomerPointsRecords(long customerId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='BONUS_POINTS' AND VALUE LIKE ?",
                recordMapper, "%customerId\":" + customerId + "%");
    }

    public List<CustomerPointsRecord> getCustomerPointsRecordsSortedFromEarliest(long customerId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='BONUS_POINTS' AND VALUE LIKE ? ORDER BY CREATED_AT",
                recordMapper, "%\"customerId\":" + customerId + "%");
    }
}
