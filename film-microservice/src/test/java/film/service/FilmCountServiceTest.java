package film.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import film.domain.Film;
import film.repository.FilmRepository;
import film.repository.rental.RentalRepository;
import film.repository.rental.record._return.ReturnRecord;
import film.repository.rental.record.rental.RentalRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class FilmCountServiceTest {
    private FilmRepository filmRepository;
    private RentalRepository rentalRepository;
    private FilmCountService service;
    private Film film;

    @Before
    public void setUp() {
        this.filmRepository = Mockito.mock(FilmRepository.class);
        this.rentalRepository = Mockito.mock(RentalRepository.class);
        this.service = new FilmCountService(filmRepository, rentalRepository);
        this.film = new Film(1L, "Matrix 11", LocalDate.of(2018, 3, 19), 10);
    }

    @Test
    public void testNotFoundFilmId() {
        when(filmRepository.findById(any())).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), service.getFilmCount(1L));
    }

    @Test
    public void testNoRentalsNorReturns() {
        when(filmRepository.findById(any())).thenReturn(Optional.of(film));
        when(rentalRepository.getRentals(anyLong())).thenReturn(Collections.emptyList());
        when(rentalRepository.getReturns(anyLong())).thenReturn(Collections.emptyList());

        assertEquals(Optional.of(10), service.getFilmCount(1L));
    }

    @Test
    public void testRentalsAndNoReturns() {
        when(filmRepository.findById(any())).thenReturn(Optional.of(film));

        List<RentalRecord> rentalRecords = ImmutableList.of(
                new RentalRecord(ImmutableList.of(
                        new RentalRecord.FilmRental(1, 2),
                        new RentalRecord.FilmRental(2, 1))),
                new RentalRecord(ImmutableList.of(
                        new RentalRecord.FilmRental(1, 1)
                ))
        );
        when(rentalRepository.getRentals(anyLong())).thenReturn(rentalRecords);

        assertEquals(Optional.of(7), service.getFilmCount(1L));
    }

    @Test
    public void testRentalsAndReturns() {
        when(filmRepository.findById(any())).thenReturn(Optional.of(film));

        List<RentalRecord> rentalRecords = ImmutableList.of(
                new RentalRecord(ImmutableList.of(
                        new RentalRecord.FilmRental(1, 2),
                        new RentalRecord.FilmRental(2, 1))),
                new RentalRecord(ImmutableList.of(
                        new RentalRecord.FilmRental(1, 1)
                ))
        );
        when(rentalRepository.getRentals(anyLong())).thenReturn(rentalRecords);

        List<ReturnRecord> returnRecords = ImmutableList.of(
                new ReturnRecord(ImmutableMap.of(1L, 1, 2L, 1)),
                new ReturnRecord(ImmutableMap.of(1L, 1)));
        when(rentalRepository.getReturns(anyLong())).thenReturn(returnRecords);

        assertEquals(Optional.of(9), service.getFilmCount(1L));
    }
}
