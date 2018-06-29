package customer.domain;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CustomerPointsRecord {
    private int points;
    private ZonedDateTime dateTime;
}
