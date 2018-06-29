package film.repository.rental.record._return;


import film.repository.rental.record.Record;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRecord implements Record {
    private Map<Long, Integer> filmIdsToCounts;
}
