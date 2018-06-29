package rental.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rental.domain.event.Rental;
import rental.domain.event.Return;
import rental.service.RentalService;
import rental.service.ReturnService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final ReturnService returnService;

    @PostMapping("/rentals")
    public void rent(@Valid @RequestBody Rental request) {
        List<String> validationErrors = validateRentalRequest(request);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        rentalService.apply(request);
    }

    @PostMapping("/returns")
    public void _return(@Valid @RequestBody Return request) {
        List<String> validationErrors = validateReturnRequest(request.getFilmIdsToCounts());
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        returnService.apply(request);
    }

    private List<String> validateRentalRequest(Rental request) {
        List<String> errors = new ArrayList<>();

        Set<Long> rentalFilmIds = request.getRentals().stream()
                .map(Rental.FilmRental::getFilmId)
                .collect(Collectors.toSet());

        rentalFilmIds.stream()
                .filter(id -> !request.getFilmIdsToTypes().containsKey(id))
                .forEach(id -> errors.add("Film id " + id + " was not found in the filmIdsToTypes property."));

        request.getFilmIdsToTypes().keySet().stream()
                .filter(id -> !rentalFilmIds.contains(id))
                .forEach(id -> errors.add("Film id " + id + " from filmIdsToTypes was not found in the rental film ids."));

        request.getFilmIdsToTypes().entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .forEach(entry -> errors.add("Film id " + entry.getKey() + " was provided with a null film type."));

        return errors;
    }

    private List<String> validateReturnRequest(Map<Long, Integer> filmIdsToCounts) {
        List<String> errors = new ArrayList<>();

        filmIdsToCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .forEach(entry -> errors.add("Film id " + entry.getKey() + " was provided with a null count."));

        filmIdsToCounts.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue() < 0)
                .forEach(entry -> errors.add("Film id " + entry.getKey() + " was provided with negative count."));

        return errors;
    }
}
