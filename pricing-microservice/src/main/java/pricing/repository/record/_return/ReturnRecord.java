package pricing.repository.record._return;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pricing.repository.record.Record;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRecord implements Record {
    private Map<Long, Integer> filmIdsToCounts;
}
