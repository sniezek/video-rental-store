package pricing.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import pricing.domain.FilmType;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RentalPricingRequest {
    @NotEmpty
    @Valid
    private List<Rental> rentals;
    private Map<Long, FilmType> filmIdsToTypes;

    @Data
    @AllArgsConstructor
    public static class Rental {
        @NotNull
        private Long filmId;
        @NotNull
        @Positive
        private Integer filmCount;
        @NotNull
        @Positive
        private Integer days;
    }
}
