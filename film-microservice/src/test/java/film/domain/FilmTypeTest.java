package film.domain;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FilmTypeTest {
    @Test
    public void testFromPremiereNew() {
        LocalDate now = LocalDate.now();

        assertEquals(FilmType.NEW, FilmType.fromPremiere(now));
        assertEquals(FilmType.NEW, FilmType.fromPremiere(now.minusDays(1)));
        assertEquals(FilmType.NEW, FilmType.fromPremiere(now.plusDays(1)));
    }

    @Test
    public void testFromPremiereRegular() {
        LocalDate now = LocalDate.now();

        int minimumDaysAfterPremiere = FilmType.REGULAR.getMinimumDaysAfterPremiere();

        assertEquals(FilmType.REGULAR, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere)));
        assertEquals(FilmType.REGULAR, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere + 1)));
        assertNotEquals(FilmType.REGULAR, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere - 1)));
    }

    @Test
    public void testFromPremiereOld() {
        LocalDate now = LocalDate.now();

        int minimumDaysAfterPremiere = FilmType.OLD.getMinimumDaysAfterPremiere();

        assertEquals(FilmType.OLD, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere)));
        assertEquals(FilmType.OLD, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere + 1)));
        assertNotEquals(FilmType.OLD, FilmType.fromPremiere(now.minusDays(minimumDaysAfterPremiere - 1)));
    }
}
