package pricing.repository.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public abstract class RecordMapper {
    private final ObjectMapper objectMapper;

    protected <R extends Record> R mapRow(Class<R> clazz, ResultSet rs, RecordModifier<R> modifier) throws SQLException {
        try {
            R record = objectMapper.readValue(rs.getString("VALUE"), clazz);
            modifier.modify(record);
            return record;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    protected <R extends Record> R mapRow(Class<R> clazz, ResultSet rs) throws SQLException {
        return mapRow(clazz, rs, record -> {});
    }

    protected ZonedDateTime getRecordDateTime(ResultSet rs) throws SQLException {
        return ZonedDateTime.ofInstant(rs.getTimestamp("CREATED_AT").toInstant(), ZoneOffset.UTC);
    }
}
