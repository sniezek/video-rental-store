package pricing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pricing.domain.FilmType;
import pricing.domain.request.ReturnPricingRequest;
import pricing.domain.response.ReturnPricingResponse;
import pricing.repository.RentalRepository;
import pricing.repository.record.rental.RentalRecord;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ReturnPricingService implements PricingService<ReturnPricingRequest> {
    private final RentalRepository rentalRepository;

    @Override
    public ReturnPricingResponse getPricingResponse(ReturnPricingRequest request) {
        ReturnPricingResponse.Builder builder = ReturnPricingResponse.builder();

        BigDecimal extraDaysCharge = getExtraDaysCharge(request, builder);
        return builder.withPrice(extraDaysCharge).build();
    }

    private BigDecimal getExtraDaysCharge(ReturnPricingRequest request, ReturnPricingResponse.Builder builder) {
        Set<Long> filmIds = request.getFilmIdsToCounts().keySet();

        Map<Long, List<RentalRecord>> filmIdsToRentalRecordsSortedFromEarliest = filmIds.stream()
                .collect(toMap(filmId -> filmId,
                        filmId -> rentalRepository.getRentalsSortedFromEarliest(request.getCustomerId(), filmId)));
        Map<Long, Integer> filmIdsToTotalPreviouslyReturned = filmIds.stream()
                .collect(toMap(filmId -> filmId,
                        filmId -> rentalRepository.getReturns(request.getCustomerId(), filmId).stream()
                                .mapToInt(returnRecord -> returnRecord.getFilmIdsToCounts().get(filmId))
                                .sum())
                );
        Map<Long, Integer> filmIdsToCounts = request.getFilmIdsToCounts();

        return calculateCharge(filmIdsToRentalRecordsSortedFromEarliest, filmIdsToTotalPreviouslyReturned, filmIdsToCounts, builder);
    }

    private BigDecimal calculateCharge(Map<Long, List<RentalRecord>> filmIdsToRentalRecordListsSortedFromEarliest,
                                       Map<Long, Integer> filmIdsToTotalPreviouslyReturned,
                                       Map<Long, Integer> filmIdsToCounts,
                                       ReturnPricingResponse.Builder builder) {
        BigDecimal totalCharge = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : filmIdsToTotalPreviouslyReturned.entrySet()) {
            long filmId = entry.getKey();
            int totalPreviouslyReturnedCount = entry.getValue();
            int leftBroughtCopiesToReturn = filmIdsToCounts.get(filmId);
            List<RentalRecord> rentalRecords = filmIdsToRentalRecordListsSortedFromEarliest.get(filmId);

            BigDecimal singleFilmCharge = calculateSingleFilmCharge(filmId, totalPreviouslyReturnedCount, rentalRecords, leftBroughtCopiesToReturn, builder);
            totalCharge = totalCharge.add(singleFilmCharge);
        }

        return totalCharge;
    }

    private BigDecimal calculateSingleFilmCharge(long filmId,
                                                 int totalPreviouslyReturnedCount,
                                                 List<RentalRecord> rentalRecords,
                                                 int leftBroughtCopiesToReturn,
                                                 ReturnPricingResponse.Builder builder) {
        BigDecimal singleFilmCharge = BigDecimal.ZERO;

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        int totalPreviouslyRentedCount = 0;

        for (RentalRecord rentalRecord : rentalRecords) {
            List<RentalRecord.FilmRental> rentalsWithinRecord = rentalRecord.getRentals().stream()
                    .filter(rental -> rental.getFilmId() == filmId)
                    .collect(toList());

            int rentedCount = rentalsWithinRecord.stream()
                    .mapToInt(RentalRecord.FilmRental::getFilmCount)
                    .sum();
            totalPreviouslyRentedCount += rentedCount;
            if (totalPreviouslyRentedCount <= totalPreviouslyReturnedCount) {
                continue;
            }

            FilmType filmTypeAtRentalDay = rentalRecord.getFilmIdsToTypes().get(filmId);
            long daysHeld = DAYS.between(rentalRecord.getDateTime(), now);
            List<RentalRecord.FilmRental> rentalsWithinRecordSortedByDaysDeclared = rentalsWithinRecord.stream()
                    .sorted(comparingInt(RentalRecord.FilmRental::getDays))
                    .collect(toList());

            for (RentalRecord.FilmRental rental : rentalsWithinRecordSortedByDaysDeclared) {
                long extraDays = daysHeld - rental.getDays();

                if (leftBroughtCopiesToReturn == 0 || extraDays <= 0) {
                    return singleFilmCharge;
                }

                int count = Math.min(rental.getFilmCount(), leftBroughtCopiesToReturn);

                BigDecimal priceForOne = filmTypeAtRentalDay.getLatePricingStrategy().apply((int) extraDays);
                singleFilmCharge = singleFilmCharge.add(priceForOne).multiply(BigDecimal.valueOf(count));

                builder.withMessage("Film id: " + filmId + ", extra days: " + extraDays + ", price for one: " +
                        priceForOne + ", count: " + count + ", price for all: " + singleFilmCharge);

                leftBroughtCopiesToReturn -= count;
            }
        }

        return singleFilmCharge;
    }
}
