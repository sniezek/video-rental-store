package rental.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import rental.repository.EventValue;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class Return implements EventValue {
    @NotNull
    private Long customerId;
    @NotEmpty
    private Map<Long, Integer> filmIdsToCounts;
    @NotNull
    @Min(value = 0)
    private BigDecimal price;
}
