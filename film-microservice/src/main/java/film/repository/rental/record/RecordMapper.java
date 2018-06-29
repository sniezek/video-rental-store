package film.repository.rental.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public abstract class RecordMapper {
    private final ObjectMapper objectMapper;

    protected <R extends Record> R mapRow(Class<R> clazz, ResultSet rs) throws SQLException {
        try {
            return objectMapper.readValue(rs.getString("VALUE"), clazz);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
