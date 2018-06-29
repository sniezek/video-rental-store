package pricing.repository.record._return;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import pricing.repository.record.RecordMapper;

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
