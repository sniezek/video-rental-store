package film.service;

import film.domain.Film;
import film.domain.FilmType;
import film.repository.FilmRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FilmTypeServiceTest {
    private FilmRepository filmRepository;
    private FilmTypeService service;
    private Film film;

    @Before
    public void setUp() {
        this.filmRepository = Mockito.mock(FilmRepository.class);
        this.service = new FilmTypeService(filmRepository);
    }

    @Test
    public void testNotFoundFilmId() {
        when(filmRepository.findById(any())).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), service.getFilmType(1L));
    }

    @Test
    public void test() {
        when(filmRepository.findById(any())).thenReturn(Optional.of(new Film(1L, "Matrix 11", LocalDate.now(), 0)));
        assertEquals(Optional.of(FilmType.NEW), service.getFilmType(1L));
    }
}
