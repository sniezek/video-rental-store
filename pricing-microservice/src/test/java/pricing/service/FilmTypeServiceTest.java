package pricing.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.response.RentalPricingResponse;

import static org.assertj.core.api.Assertions.assertThat;


public class FilmTypeServiceTest {
    private FilmTypeService service;

    @Before
    public void setUp() throws Exception {
        this.service = new FilmTypeService(null, null);
    }

    @Test
    public void testHandleErrorCallingService() {
        RentalPricingResponse.Builder builder = RentalPricingResponse.builder();
        RentalPricingRequest request = new RentalPricingRequest(
                ImmutableList.of(
                        new RentalPricingRequest.Rental(1L, 1, 1),
                        new RentalPricingRequest.Rental(2L, 1, 5),
                        new RentalPricingRequest.Rental(3L, 1, 2),
                        new RentalPricingRequest.Rental(4L, 1, 7)
                ), null);
        assertThat(service.getFilmIdsToTypes(request, builder)).isEmpty();
        assertThat(builder.build().getMessages()).containsOnly("Error while calling the film service.");
    }

    // other cases tested in RentalPricingServiceTest
}