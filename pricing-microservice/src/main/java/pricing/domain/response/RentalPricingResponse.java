package pricing.domain.response;

import lombok.Getter;
import pricing.domain.FilmType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentalPricingResponse extends PricingResponse {
    @Getter
    private final Map<Long, FilmType> filmIdsToTypes;

    RentalPricingResponse(BigDecimal price, List<String> messages, Map<Long, FilmType> filmIdsToTypes) {
        super(price, messages);
        this.filmIdsToTypes = filmIdsToTypes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends PricingResponse.Builder<Builder> {
        private final Map<Long, FilmType> filmIdsToTypes;

        private Builder() {
            this.filmIdsToTypes = new HashMap<>();
        }

        @Override
        Builder getThis() {
            return this;
        }

        public Builder withFilmIdsToFilmTypes(Map<Long, FilmType> filmIdsToTypes) {
            this.filmIdsToTypes.putAll(filmIdsToTypes);
            return this;
        }

        public RentalPricingResponse build() {
            return new RentalPricingResponse(price, messages, filmIdsToTypes);
        }
    }
}
