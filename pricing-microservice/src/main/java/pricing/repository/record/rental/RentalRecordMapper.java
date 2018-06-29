package pricing.repository.record.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import pricing.repository.record.RecordMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RentalRecordMapper extends RecordMapper implements RowMapper<RentalRecord> {
    public RentalRecordMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public RentalRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(RentalRecord.class, rs, record -> record.setDateTime(getRecordDateTime(rs)));
    }
}
