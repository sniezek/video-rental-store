package pricing.repository.record;

import java.sql.SQLException;

@FunctionalInterface
public interface RecordModifier<T extends Record> {
    void modify(T t) throws SQLException;
}
