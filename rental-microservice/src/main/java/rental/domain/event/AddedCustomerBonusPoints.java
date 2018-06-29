package rental.domain.event;

import lombok.Data;
import rental.repository.EventValue;

@Data
public class AddedCustomerBonusPoints implements EventValue {
    private final long customerId;
    private final int points;
}
