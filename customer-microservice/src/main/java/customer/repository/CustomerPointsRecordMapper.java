package customer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import customer.domain.CustomerPointsRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class CustomerPointsRecordMapper implements RowMapper<CustomerPointsRecord> {
    private final ObjectMapper objectMapper;

    CustomerPointsRecordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CustomerPointsRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            CustomerPointsRecord record = objectMapper.readValue(rs.getString("VALUE"), CustomerPointsRecord.class);
            record.setDateTime(ZonedDateTime.ofInstant(rs.getTimestamp("CREATED_AT").toInstant(), ZoneOffset.UTC));
            return record;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
