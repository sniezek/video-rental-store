package rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionTemplate;
import rental.domain.FilmType;
import rental.domain.event.Rental;
import rental.repository.EventStore;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RentalServiceTest {
    private RentalService rentalService;

    @Before
    public void setUp() throws Exception {
        this.rentalService = new RentalService(Mockito.mock(EventStore.class), Mockito.mock(ObjectMapper.class), Mockito.mock(TransactionTemplate.class));
    }

    @Test
    public void testCalculateCustomerBonusPoints() {
        Rental request = new Rental(1L,
                ImmutableList.of(
                        new Rental.FilmRental(1L, 1, 1),
                        new Rental.FilmRental(1L, 1, 2),
                        new Rental.FilmRental(2L, 2, 1),
                        new Rental.FilmRental(3L, 2, 1)),
                BigDecimal.valueOf(240.0),
                ImmutableMap.of(1L, FilmType.NEW, 2L, FilmType.REGULAR, 3L, FilmType.OLD));

        int expected = 2 * FilmType.NEW.getCustomerBonusPointsForRental() +
                2 * FilmType.REGULAR.getCustomerBonusPointsForRental() +
                2 * FilmType.OLD.getCustomerBonusPointsForRental();
        assertEquals(expected, rentalService.calculateCustomerBonusPoints(request));
    }
}
