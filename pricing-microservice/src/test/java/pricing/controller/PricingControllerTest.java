package pricing.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pricing.domain.FilmType;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.request.ReturnPricingRequest;
import pricing.domain.response.PricingResponse;
import pricing.domain.response.RentalPricingResponse;
import pricing.domain.response.ReturnPricingResponse;
import pricing.service.RentalPricingService;
import pricing.service.ReturnPricingService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PricingControllerTest {
    private RentalPricingService rentalPricingService;
    private ReturnPricingService returnPricingService;
    private PricingController pricingController;

    @Before
    public void setUp() throws Exception {
        this.rentalPricingService = Mockito.mock(RentalPricingService.class);
        this.returnPricingService = Mockito.mock(ReturnPricingService.class);
        this.pricingController = new PricingController(rentalPricingService, returnPricingService);
    }

    @Test
    public void testRental() {
        RentalPricingRequest request = new RentalPricingRequest(ImmutableList.of(new RentalPricingRequest.Rental(1L, 1, 1)), ImmutableMap.of(2L, FilmType.OLD));

        ResponseEntity<PricingResponse> responseEntity = pricingController.getRentalPricingResponse(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        PricingResponse response = responseEntity.getBody();
        assertNull(response.getPrice());
        assertThat(response.getMessages()).containsOnly("Film id 2 from filmIdsToTypes was not found in the rental film ids.");
    }

    @Test
    public void testRentalOk() {
        RentalPricingRequest request = new RentalPricingRequest(ImmutableList.of(new RentalPricingRequest.Rental(1L, 1, 1)), ImmutableMap.of(1L, FilmType.OLD));
        RentalPricingResponse pricingResponse = RentalPricingResponse.builder().withPrice(new BigDecimal(123)).build();
        when(rentalPricingService.getPricingResponse(request)).thenReturn(pricingResponse);
        ResponseEntity<PricingResponse> responseEntity = pricingController.getRentalPricingResponse(request);

        verify(rentalPricingService, times(1)).getPricingResponse(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pricingResponse, responseEntity.getBody());
    }

    @Test
    public void testReturn() {
        Map<Long, Integer> filmIdsToCounts = new HashMap<>();
        filmIdsToCounts.put(1L, null);
        filmIdsToCounts.put(2L, -1);
        ReturnPricingRequest request = new ReturnPricingRequest(1L, filmIdsToCounts);

        ResponseEntity<PricingResponse> responseEntity = pricingController.getReturnPricingResponse(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        PricingResponse response = responseEntity.getBody();

        assertNull(response.getPrice());
        assertThat(response.getMessages()).containsOnly("Film id 1 was provided with a null count.", "Film id 2 was provided with negative count.");
    }

    @Test
    public void testReturnOk() {
        ReturnPricingRequest request = new ReturnPricingRequest(1L, ImmutableMap.of(1L, 1));

        ReturnPricingResponse pricingResponse = ReturnPricingResponse.builder().withPrice(new BigDecimal(123)).build();
        when(returnPricingService.getPricingResponse(request)).thenReturn(pricingResponse);
        ResponseEntity<PricingResponse> responseEntity = pricingController.getReturnPricingResponse(request);

        verify(returnPricingService, times(1)).getPricingResponse(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(pricingResponse, responseEntity.getBody());
    }
}