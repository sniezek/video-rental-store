package rental.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import rental.domain.FilmType;
import rental.repository.EventValue;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Rental implements EventValue {
    @NotNull
    private Long customerId;
    @NotEmpty
    @Valid
    private List<FilmRental> rentals;
    @NotNull
    @Min(value = 0)
    private BigDecimal price;
    @NotEmpty
    private Map<Long, FilmType> filmIdsToTypes;

    @Data
    @AllArgsConstructor
    public static class FilmRental {
        @NotNull
        private Long filmId;
        @NotNull
        @Min(value = 1)
        private Integer filmCount;
        @NotNull
        @Min(value = 1)
        private Integer days;
    }
}
