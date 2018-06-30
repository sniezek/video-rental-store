package pricing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.request.ReturnPricingRequest;
import pricing.domain.response.PricingResponse;
import pricing.domain.response.RentalPricingResponse;
import pricing.service.PricingService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
@RequiredArgsConstructor
public class PricingController {
    private final PricingService<RentalPricingRequest> rentalPricingService;
    private final PricingService<ReturnPricingRequest> returnPricingService;

    @PostMapping("/rentals")
    public ResponseEntity<PricingResponse> getRentalPricingResponse(@Valid @RequestBody RentalPricingRequest request) {
        Collection<String> validationErrors = validateRentalPricingRequest(request);
        if (!validationErrors.isEmpty()) {
            PricingResponse response = RentalPricingResponse.builder().withMessages(validationErrors).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        PricingResponse response = rentalPricingService.getPricingResponse(request);
        return new ResponseEntity<>(response, getStatus(response));
    }

    @PostMapping("/returns")
    public ResponseEntity<PricingResponse> getReturnPricingResponse(@Valid @RequestBody ReturnPricingRequest request) {
        Collection<String> validationErrors = validateReturnPricingRequest(request);
        if (!validationErrors.isEmpty()) {
            PricingResponse response = RentalPricingResponse.builder().withMessages(validationErrors).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        PricingResponse response = returnPricingService.getPricingResponse(request);
        return new ResponseEntity<>(response, getStatus(response));
    }

    private Collection<String> validateRentalPricingRequest(RentalPricingRequest request) {
        if (request.getFilmIdsToTypes() == null) {
            return Collections.emptySet();
        }
        Set<Long> rentalFilmIds = request.getRentals().stream()
                .map(RentalPricingRequest.Rental::getFilmId)
                .collect(Collectors.toSet());

        return request.getFilmIdsToTypes().keySet().stream()
                .filter(id -> !rentalFilmIds.contains(id))
                .map(id -> "Film id " + id + " from filmIdsToTypes was not found in the rental film ids.")
                .collect(Collectors.toSet());
    }

    private Collection<String> validateReturnPricingRequest(ReturnPricingRequest request) {
        Set<String> validationErrors = new HashSet<>();
        request.getFilmIdsToCounts().forEach((key, value) -> {
            if (value == null) {
                validationErrors.add("Film id " + key + " was provided with a null count.");
            } else if (value < 0) {
                validationErrors.add("Film id " + key + " was provided with negative count.");
            }
        });
        return validationErrors;
    }

    private static HttpStatus getStatus(PricingResponse response) {
        return response.getPrice() == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
    }
}
