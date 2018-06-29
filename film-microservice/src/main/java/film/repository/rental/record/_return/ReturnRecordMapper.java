package film.repository.rental.record._return;

import com.fasterxml.jackson.databind.ObjectMapper;
import film.repository.rental.record.RecordMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnRecordMapper extends RecordMapper implements RowMapper<ReturnRecord> {
    public ReturnRecordMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public ReturnRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapRow(ReturnRecord.class, rs);
    }
}
