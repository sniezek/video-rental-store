package pricing.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import pricing.domain.FilmType;
import pricing.domain.Price;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.response.RentalPricingResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RentalPricingServiceTest {
    private String filmServiceAddress;
    private AsyncRestTemplate asyncRestTemplate;
    private RentalPricingService service;

    @Before
    public void setUp() throws Exception {
        this.filmServiceAddress = "someAddress";
        this.asyncRestTemplate = Mockito.mock(AsyncRestTemplate.class);
        this.service = new RentalPricingService(new FilmTypeService(filmServiceAddress, asyncRestTemplate));

        assertEquals(BigDecimal.valueOf(40.0), Price.PREMIUM.get());
        assertEquals(BigDecimal.valueOf(30.0), Price.BASIC.get());
    }

    @Test
    public void testWhenFilmIdsToTypesProvidedAndSingleFilmCounts() {
        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(2L, 1, 5),
                        new RentalPricingRequest.Rental(3L, 1, 2),
                        new RentalPricingRequest.Rental(4L, 1, 7)
                ),
                ImmutableMap.of(1L, FilmType.NEW, 2L, FilmType.REGULAR, 3L, FilmType.REGULAR, 4L, FilmType.OLD));

        RentalPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(250.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, days: 1, price for one: 40.0, count: 1, price for all: 40.0",
                "Film id: 2, days: 5, price for one: 90.0, count: 1, price for all: 90.0",
                "Film id: 3, days: 2, price for one: 30.0, count: 1, price for all: 30.0",
                "Film id: 4, days: 7, price for one: 90.0, count: 1, price for all: 90.0");
    }

    @Test
    public void testWhenFilmIdsToTypesProvidedAndSomeFilmCountsNotSingle() {
        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 2, 1),
                        new RentalPricingRequest.Rental(2L, 1, 5),
                        new RentalPricingRequest.Rental(3L, 2, 2),
                        new RentalPricingRequest.Rental(4L, 1, 7)
                ),
                ImmutableMap.of(1L, FilmType.NEW, 2L, FilmType.REGULAR, 3L, FilmType.REGULAR, 4L, FilmType.OLD));

        RentalPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(320.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, days: 1, price for one: 40.0, count: 2, price for all: 80.0",
                "Film id: 2, days: 5, price for one: 90.0, count: 1, price for all: 90.0",
                "Film id: 3, days: 2, price for one: 30.0, count: 2, price for all: 60.0",
                "Film id: 4, days: 7, price for one: 90.0, count: 1, price for all: 90.0");
    }

    @Test
    public void testWhenNoFilmIdsToTypesProvidedAndAllOkResponses() throws ExecutionException, InterruptedException {
        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(2L, 1, 5),
                        new RentalPricingRequest.Rental(3L, 1, 2),
                        new RentalPricingRequest.Rental(4L, 1, 7)
                ), null);

        ListenableFuture newFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(newFilmTypeFuture.get()).thenReturn(ResponseEntity.ok(FilmType.NEW.toString()));
        doReturn(newFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/1/type"), any());

        ListenableFuture regularFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(regularFilmTypeFuture.get()).thenReturn(ResponseEntity.ok(FilmType.REGULAR.toString()));
        doReturn(regularFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/2/type"), any());
        doReturn(regularFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/3/type"), any());

        ListenableFuture oldFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(oldFilmTypeFuture.get()).thenReturn(ResponseEntity.ok(FilmType.OLD.toString()));
        doReturn(oldFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());

        RentalPricingResponse response = service.getPricingResponse(request);

        assertEquals(BigDecimal.valueOf(250.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, days: 1, price for one: 40.0, count: 1, price for all: 40.0",
                "Film id: 2, days: 5, price for one: 90.0, count: 1, price for all: 90.0",
                "Film id: 3, days: 2, price for one: 30.0, count: 1, price for all: 30.0",
                "Film id: 4, days: 7, price for one: 90.0, count: 1, price for all: 90.0");
    }

    @Test
    public void testWenFilmIdsToTypesProvidedPartiallyAndTwoDifferentRentalsOfSameFilm() throws
            ExecutionException, InterruptedException {
        Map<Long, FilmType> requestFilmIdsToTypes = new HashMap<>();
        requestFilmIdsToTypes.put(1L, FilmType.NEW);
        requestFilmIdsToTypes.put(3L, null);

        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(2L, 1, 5),
                        new RentalPricingRequest.Rental(3L, 1, 2),
                        new RentalPricingRequest.Rental(4L, 1, 7),
                        new RentalPricingRequest.Rental(4L, 1, 6)
                ), requestFilmIdsToTypes);

        ListenableFuture regularFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(regularFilmTypeFuture.get()).thenReturn(ResponseEntity.ok(FilmType.REGULAR.toString()));
        doReturn(regularFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/2/type"), any());
        doReturn(regularFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/3/type"), any());

        ListenableFuture oldFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(oldFilmTypeFuture.get()).thenReturn(ResponseEntity.ok(FilmType.OLD.toString()));
        doReturn(oldFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());

        RentalPricingResponse response = service.getPricingResponse(request);

        verify(asyncRestTemplate, times(0)).getForEntity(eq(filmServiceAddress + "/films/1/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/2/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/3/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());

        assertEquals(BigDecimal.valueOf(310.0), response.getPrice());
        assertThat(response.getMessages()).containsOnly(
                "Film id: 1, days: 1, price for one: 40.0, count: 1, price for all: 40.0",
                "Film id: 2, days: 5, price for one: 90.0, count: 1, price for all: 90.0",
                "Film id: 3, days: 2, price for one: 30.0, count: 1, price for all: 30.0",
                "Film id: 4, days: 7, price for one: 90.0, count: 1, price for all: 90.0",
                "Film id: 4, days: 6, price for one: 60.0, count: 1, price for all: 60.0");
    }

    @Test
    public void testWhenFilmIdsToTypesProvidedPartiallyAndTwoDifferentRentalsOfSameFilmAndErrorResponses() throws
            ExecutionException, InterruptedException {
        Map<Long, FilmType> requestFilmIdsToTypes = new HashMap<>();
        requestFilmIdsToTypes.put(1L, FilmType.NEW);
        requestFilmIdsToTypes.put(4L, null);

        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(4L, 1, 7),
                        new RentalPricingRequest.Rental(4L, 1, 6),
                        new RentalPricingRequest.Rental(5L, 1, 1)
                ), requestFilmIdsToTypes);

        ListenableFuture otherErrorFuture = Mockito.mock(ListenableFuture.class);
        when(otherErrorFuture.get()).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        doReturn(otherErrorFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());

        ListenableFuture filmNotFoundFuture = Mockito.mock(ListenableFuture.class);
        when(filmNotFoundFuture.get()).thenReturn(new ResponseEntity<>("Film not found.", HttpStatus.NOT_FOUND));
        doReturn(filmNotFoundFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/5/type"), any());

        RentalPricingResponse response = service.getPricingResponse(request);

        assertNull(response.getPrice());
        assertThat(response.getMessages()).containsOnly("Film id not found: 5", "Film id 4 response code: 500");

        verify(asyncRestTemplate, times(0)).getForEntity(eq(filmServiceAddress + "/films/1/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/5/type"), any());
    }

    @Test
    public void testWhenFilmIdsToTypesProvidedPartiallyAndTwoDifferentRentalsOfSameFilmAndUnknownFilmType() throws
            ExecutionException, InterruptedException {
        Map<Long, FilmType> requestFilmIdsToTypes = new HashMap<>();
        requestFilmIdsToTypes.put(1L, FilmType.NEW);
        requestFilmIdsToTypes.put(4L, null);

        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(4L, 1, 7),
                        new RentalPricingRequest.Rental(4L, 1, 6),
                        new RentalPricingRequest.Rental(5L, 1, 1)
                ), requestFilmIdsToTypes);

        ListenableFuture unknownFilmTypeFuture = Mockito.mock(ListenableFuture.class);
        when(unknownFilmTypeFuture.get()).thenReturn(ResponseEntity.ok("UNEXPECTED FILM TYPE NAME"));
        doReturn(unknownFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());
        doReturn(unknownFilmTypeFuture).when(asyncRestTemplate).getForEntity(eq(filmServiceAddress + "/films/5/type"), any());


        RentalPricingResponse response = service.getPricingResponse(request);

        assertNull(response.getPrice());
        assertThat(response.getMessages()).containsOnly("Unknown film type: UNEXPECTED FILM TYPE NAME for film id: 4", "Unknown film type: UNEXPECTED FILM TYPE NAME for film id: 5");
        verify(asyncRestTemplate, times(0)).getForEntity(eq(filmServiceAddress + "/films/1/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/4/type"), any());
        verify(asyncRestTemplate, times(1)).getForEntity(eq(filmServiceAddress + "/films/5/type"), any());
    }
}
