package film.repository.rental.record.rental;

import film.repository.rental.record.Record;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalRecord implements Record {
    private List<FilmRental> rentals;

    @Data
    @AllArgsConstructor
    public static class FilmRental {
        private long filmId;
        private int filmCount;
    }
}
