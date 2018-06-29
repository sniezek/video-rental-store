package pricing.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pricing.domain.FilmType;
import pricing.domain.Price;
import pricing.domain.request.ReturnPricingRequest;
import pricing.domain.response.ReturnPricingResponse;
import pricing.repository.RentalRepository;
import pricing.repository.record._return.ReturnRecord;
import pricing.repository.record.rental.RentalRecord;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ReturnPricingServiceTest {
    private RentalRepository repository;
    private ReturnPricingService service;

    @Before
    public void setUp() throws Exception {
        this.repository = Mockito.mock(RentalRepository.class);
        this.service = new ReturnPricingService(repository);

        assertEquals(BigDecimal.valueOf(40.0), Price.PREMIUM.get());
        assertEquals(BigDecimal.valueOf(30.0), Price.BASIC.get());
    }

    @Test
    public void testNormalCustomer() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime dayRented = now.minusDays(3);

        RentalRecord matrix = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(1L, 1, 1)),
                ImmutableMap.of(1L, FilmType.NEW),
                dayRented);
        RentalRecord spiderMan = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(2L, 1, 5)),
                ImmutableMap.of(2L, FilmType.REGULAR),
                dayRented);
        RentalRecord spiderMan2 = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(3L, 1, 2)),
                ImmutableMap.of(3L, FilmType.REGULAR),
                dayRented);
        RentalRecord outOfAfrica = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(4L, 1, 7)),
                ImmutableMap.of(4L, FilmType.OLD),
                dayRented);

        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(1L))).thenReturn(ImmutableList.of(matrix));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(2L))).thenReturn(ImmutableList.of(spiderMan));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(3L))).thenReturn(ImmutableList.of(spiderMan2));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(4L))).thenReturn(ImmutableList.of(outOfAfrica));

        when(repository.getReturns(anyLong(), anyLong())).thenReturn(ImmutableList.of());

        ReturnPricingRequest request = new ReturnPricingRequest(1L, ImmutableMap.of(1L, 1, 2L, 1, 3L, 1, 4L, 1));

        ReturnPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(110.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, extra days: 2, price for one: 80.0, count: 1, price for all: 80.0",
                "Film id: 3, extra days: 1, price for one: 30.0, count: 1, price for all: 30.0");
    }

    @Test
    public void testDifficultCustomer() {
        ZonedDateTime dayTwo = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime dayOne = dayTwo.minusDays(1);
        ZonedDateTime dayZero = dayTwo.minusDays(2);

        RentalRecord dayZeroRental = new RentalRecord(
                ImmutableList.of(
                        new RentalRecord.FilmRental(1L, 2, 1),
                        new RentalRecord.FilmRental(1L, 1, 2),
                        new RentalRecord.FilmRental(2L, 2, 1)
                ),
                ImmutableMap.of(1L, FilmType.NEW, 2L, FilmType.NEW),
                dayZero);
        ReturnRecord dayOneReturn = new ReturnRecord(ImmutableMap.of(2L, 1));
        RentalRecord dayOneRental = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(1L, 1, 2)),
                ImmutableMap.of(1L, FilmType.NEW),
                dayOne);

        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(1L))).thenReturn(ImmutableList.of(dayZeroRental, dayOneRental));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(2L))).thenReturn(ImmutableList.of(dayZeroRental));

        when(repository.getReturns(anyLong(), eq(1L))).thenReturn(ImmutableList.of());
        when(repository.getReturns(anyLong(), eq(2L))).thenReturn(ImmutableList.of(dayOneReturn));

        int broughtFilmOneCopiesToReturn = 2; // client returns 2 of 2
        int broughtFilmTwoCopiesToReturn = 1; // client returns all
        ReturnPricingRequest request = new ReturnPricingRequest(1L, ImmutableMap.of(1L, broughtFilmOneCopiesToReturn, 2L, broughtFilmTwoCopiesToReturn));

        ReturnPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(120.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, extra days: 1, price for one: 40.0, count: 2, price for all: 80.0",
                "Film id: 2, extra days: 1, price for one: 40.0, count: 1, price for all: 40.0");

        broughtFilmOneCopiesToReturn = 1; // client returns 1 of 2
        broughtFilmTwoCopiesToReturn = 1; // client returns all
        request = new ReturnPricingRequest(1L, ImmutableMap.of(1L, broughtFilmOneCopiesToReturn, 2L, broughtFilmTwoCopiesToReturn));

        response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(80.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, extra days: 1, price for one: 40.0, count: 1, price for all: 40.0",
                "Film id: 2, extra days: 1, price for one: 40.0, count: 1, price for all: 40.0");
    }

    @Test
    public void testReturnPriceCheckImmediatelyAfterRentalOfNormalCustomer() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        RentalRecord matrix = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(1L, 1, 1)),
                ImmutableMap.of(1L, FilmType.NEW),
                now);
        RentalRecord spiderMan = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(2L, 1, 5)),
                ImmutableMap.of(2L, FilmType.REGULAR),
                now);
        RentalRecord spiderMan2 = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(3L, 1, 2)),
                ImmutableMap.of(3L, FilmType.REGULAR),
                now);
        RentalRecord outOfAfrica = new RentalRecord(
                ImmutableList.of(new RentalRecord.FilmRental(4L, 1, 7)),
                ImmutableMap.of(4L, FilmType.OLD),
                now);

        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(1L))).thenReturn(ImmutableList.of(matrix));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(2L))).thenReturn(ImmutableList.of(spiderMan));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(3L))).thenReturn(ImmutableList.of(spiderMan2));
        when(repository.getRentalsSortedFromEarliest(anyLong(), eq(4L))).thenReturn(ImmutableList.of(outOfAfrica));

        when(repository.getReturns(anyLong(), anyLong())).thenReturn(ImmutableList.of());

        ReturnPricingRequest request = new ReturnPricingRequest(1L, ImmutableMap.of(1L, 1, 2L, 1, 3L, 1, 4L, 1));

        ReturnPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.ZERO, response.getPrice());
        assertThat(response.getMessages()).isEmpty();
    }
}
