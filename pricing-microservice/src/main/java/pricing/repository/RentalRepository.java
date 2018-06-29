package pricing.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pricing.repository.record._return.ReturnRecord;
import pricing.repository.record._return.ReturnRecordMapper;
import pricing.repository.record.rental.RentalRecord;
import pricing.repository.record.rental.RentalRecordMapper;

import java.util.List;

@Repository
public class RentalRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RentalRecordMapper recordMapper;
    private final ReturnRecordMapper returnMapper;

    public RentalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.recordMapper = new RentalRecordMapper(objectMapper);
        this.returnMapper = new ReturnRecordMapper(objectMapper);
    }

    public List<RentalRecord> getRentalsSortedFromEarliest(long customerId, long filmId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='RENTAL' AND VALUE LIKE ? AND VALUE LIKE ? ORDER BY CREATED_AT ASC",
                recordMapper,
                "%customerId\":" + customerId + "%", "%filmId\":" + filmId + "%");
    }

    public List<ReturnRecord> getReturns(long customerId, long filmId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='RETURN' AND VALUE LIKE ? AND VALUE LIKE ?",
                returnMapper,
                "%customerId\":" + customerId + "%", "%\"" + filmId + "\":%");
    }
}
