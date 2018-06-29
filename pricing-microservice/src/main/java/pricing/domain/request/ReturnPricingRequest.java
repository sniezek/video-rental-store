package pricing.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@AllArgsConstructor
public class ReturnPricingRequest {
    @NotNull
    private Long customerId;
    @NotEmpty
    private Map<Long, Integer> filmIdsToCounts;
}
