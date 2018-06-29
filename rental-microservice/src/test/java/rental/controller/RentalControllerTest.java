package rental.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import rental.domain.FilmType;
import rental.domain.event.Rental;
import rental.domain.event.Return;
import rental.service.RentalService;
import rental.service.ReturnService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RentalControllerTest {
    private RentalController rentalController;

    @Before
    public void setUp() throws Exception {
        this.rentalController = new RentalController(Mockito.mock(RentalService.class), Mockito.mock(ReturnService.class));
    }

    @Test
    public void testRentalValidationOk() {
        Rental request = new Rental(1L,
                ImmutableList.of(new Rental.FilmRental(1L, 1, 1)),
                BigDecimal.valueOf(30.0),
                ImmutableMap.of(1L, FilmType.OLD));
        try {
            rentalController.rent(request);
        } catch (ValidationException e) {
            fail();
        }
    }

    @Test
    public void testRentalValidationAllPossibleErrors() {
        Map<Long, FilmType> filmIdsToTypes = new HashMap<>();
        filmIdsToTypes.put(1L, null);

        Rental request = new Rental(1L,
                ImmutableList.of(new Rental.FilmRental(2L, 1, 1)),
                BigDecimal.valueOf(30.0),
                filmIdsToTypes);
        try {
            rentalController.rent(request);
        } catch (ValidationException e) {
            assertEquals("Film id 2 was not found in the filmIdsToTypes property. Film id 1 from filmIdsToTypes was not found in the rental film ids. Film id 1 was provided with a null film type.", e.getMessage());
            return;
        }
        fail();
    }

    @Test
    public void testReturnValidationOk() {
        Return request = new Return(1L, ImmutableMap.of(1L, 1), BigDecimal.ZERO);
        try {
            rentalController._return(request);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testReturnValidationAllPossibleErrors() {
        Map<Long, Integer> filmIdsToCounts = new HashMap<>();
        filmIdsToCounts.put(1L, null);
        filmIdsToCounts.put(2L, -1);

        Return request = new Return(1L, filmIdsToCounts, BigDecimal.ZERO);
        try {
            rentalController._return(request);
        } catch (ValidationException e) {
            assertEquals("Film id 1 was provided with a null count. Film id 2 was provided with negative count.", e.getMessage());
            return;
        }
        fail();
    }
}