package film.repository.rental.record.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import film.repository.rental.record.RecordMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RentalRecordMapper extends RecordMapper implements RowMapper<RentalRecord> {
    public RentalRecordMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public RentalRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(RentalRecord.class, rs);
    }
}
