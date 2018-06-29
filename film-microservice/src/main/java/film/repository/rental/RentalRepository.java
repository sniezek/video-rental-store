package film.repository.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import film.repository.rental.record._return.ReturnRecord;
import film.repository.rental.record._return.ReturnRecordMapper;
import film.repository.rental.record.rental.RentalRecord;
import film.repository.rental.record.rental.RentalRecordMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    public List<RentalRecord> getRentals(long filmId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='RENTAL' AND VALUE LIKE ?",
                recordMapper, "%filmId\":" + filmId + "%");
    }

    public List<ReturnRecord> getReturns(long filmId) {
        return jdbcTemplate.query(
                "SELECT * FROM event WHERE TYPE='RETURN' AND VALUE LIKE ?",
                returnMapper, "%\"" + filmId + "\":%");
    }
}
