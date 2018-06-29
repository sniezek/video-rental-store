package pricing.repository.record.rental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pricing.domain.FilmType;
import pricing.repository.record.Record;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalRecord implements Record {
    private List<FilmRental> rentals;
    private Map<Long, FilmType> filmIdsToTypes;
    private ZonedDateTime dateTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilmRental {
        private long filmId;
        private int filmCount;
        private int days;
    }
}
