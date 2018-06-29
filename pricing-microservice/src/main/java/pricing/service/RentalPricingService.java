package pricing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pricing.domain.FilmType;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.response.RentalPricingResponse;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalPricingService implements PricingService<RentalPricingRequest> {
    private final FilmTypeService filmTypeService;

    @Override
    public RentalPricingResponse getPricingResponse(RentalPricingRequest request) {
        RentalPricingResponse.Builder builder = RentalPricingResponse.builder();

        Map<Long, FilmType> filmIdsToTypes = filmTypeService.getFilmIdsToTypes(request, builder);
        if (builder.hasMessages()) {
            return builder.build();
        }

        BigDecimal price = calculatePrice(request, filmIdsToTypes, builder);
        return builder.withFilmIdsToFilmTypes(filmIdsToTypes).withPrice(price).build();
    }

    private BigDecimal calculatePrice(RentalPricingRequest request, Map<Long, FilmType> filmIdsToFilmTypes, RentalPricingResponse.Builder builder) {
        BigDecimal totalSum = BigDecimal.ZERO;

        for (RentalPricingRequest.Rental rental : request.getRentals()) {
            BigDecimal priceForOne = filmIdsToFilmTypes.get(rental.getFilmId()).getUpfrontPricingStrategy().apply(rental.getDays());
            BigDecimal priceForUnits = priceForOne.multiply(BigDecimal.valueOf(rental.getFilmCount()));
            totalSum = totalSum.add(priceForUnits);

            builder.withMessage("Film id: " + rental.getFilmId() + ", days: " + rental.getDays() + ", price for one: " +
                    priceForOne + ", count: " + rental.getFilmCount() + ", price for all: " + priceForUnits);
        }

        return totalSum;
    }
}
