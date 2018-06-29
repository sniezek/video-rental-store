package pricing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import pricing.domain.FilmType;
import pricing.domain.request.RentalPricingRequest;
import pricing.domain.response.RentalPricingResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
class FilmTypeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilmTypeService.class);

    private final String filmServiceAddress;
    private final AsyncRestTemplate asyncRestTemplate;

    FilmTypeService(@Value("#{'${film.service.address}'}") String filmServiceAddress, AsyncRestTemplate asyncRestTemplate) {
        this.filmServiceAddress = filmServiceAddress;
        this.asyncRestTemplate = asyncRestTemplate;
    }

    Map<Long, FilmType> getFilmIdsToTypes(RentalPricingRequest request, RentalPricingResponse.Builder builder) {
        Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses;

        try {
            filmIdsToFilmTypeResponses = getFilmIdsToFilmTypesFromService(request);
        } catch (Exception e) {
            return handleErrorCallingService(e, builder);
        }
        Set<Map.Entry<Long, ResponseEntity<String>>> filmIdsToErrorResponses = getFilmIdsToErrorResponses(filmIdsToFilmTypeResponses);
        if (!filmIdsToErrorResponses.isEmpty()) {
            return handleFilmIdsToErrorResponses(filmIdsToErrorResponses, builder);
        }
        Map<Long, ResponseEntity<String>> filmIdsToUnknownFilmTypeResponses = getFilmIdsToUnknownFilmTypeResponses(filmIdsToFilmTypeResponses);
        if (!filmIdsToUnknownFilmTypeResponses.isEmpty()) {
            return handleFilmIdsToUnknownFilmTypeResponses(filmIdsToUnknownFilmTypeResponses, builder);
        }

        return getFilmIdsToTypes(request.getFilmIdsToTypes(), filmIdsToFilmTypeResponses);
    }

    private Map<Long, ResponseEntity<String>> getFilmIdsToFilmTypesFromService(RentalPricingRequest request) throws ExecutionException, InterruptedException {
        Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses = new HashMap<>();

        Map<Long, ListenableFuture<ResponseEntity<String>>> filmIdsToAsynchronousCalls = request.getRentals().stream()
                .map(RentalPricingRequest.Rental::getFilmId)
                .distinct()
                .filter(filmId -> shouldPerformFilmTypeCall(request.getFilmIdsToTypes(), filmId))
                .collect(toMap(filmId -> filmId, this::performFilmTypeCall));

        for (Map.Entry<Long, ListenableFuture<ResponseEntity<String>>> entry : filmIdsToAsynchronousCalls.entrySet()) {
            filmIdsToFilmTypeResponses.put(entry.getKey(), entry.getValue().get());
        }
        return filmIdsToFilmTypeResponses;
    }

    private boolean shouldPerformFilmTypeCall(Map<Long, FilmType> requestFilmIdsToTypes, Long filmId) {
        return requestFilmIdsToTypes == null || !requestFilmIdsToTypes.containsKey(filmId) || requestFilmIdsToTypes.get(filmId) == null;
    }

    private ListenableFuture<ResponseEntity<String>> performFilmTypeCall(Long id) {
        return asyncRestTemplate.getForEntity(filmServiceAddress + "/films/" + id + "/type", String.class);
    }

    private Map<Long, FilmType> handleErrorCallingService(Exception e, RentalPricingResponse.Builder builder) {
        LOGGER.error("Error while calling the film service.", e);
        builder.withMessage("Error while calling the film service.");
        return Collections.emptyMap();
    }

    private Set<Map.Entry<Long, ResponseEntity<String>>> getFilmIdsToErrorResponses(Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses) {
        return filmIdsToFilmTypeResponses.entrySet().stream()
                .filter(entry -> entry.getValue().getStatusCode().isError())
                .collect(toSet());
    }

    private Map<Long, FilmType> handleFilmIdsToErrorResponses(Set<Map.Entry<Long, ResponseEntity<String>>> filmIdsToErrorResponses, RentalPricingResponse.Builder builder) {
        Set<Map.Entry<Long, ResponseEntity<String>>> filmIdsToNotFoundFilmResponses = filmIdsToErrorResponses.stream()
                .filter(entry -> entry.getValue().getStatusCode() == HttpStatus.NOT_FOUND)
                .collect(toSet());

        filmIdsToNotFoundFilmResponses.stream()
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .forEach(id -> builder.withMessage("Film id not found: " + id));

        filmIdsToErrorResponses.removeAll(filmIdsToNotFoundFilmResponses);
        filmIdsToErrorResponses.forEach(entry -> builder.withMessage("Film id " + entry.getKey() + " response code: " + entry.getValue().getStatusCode()));
        return Collections.emptyMap();
    }

    private Map<Long, ResponseEntity<String>> getFilmIdsToUnknownFilmTypeResponses(Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses) {
        return filmIdsToFilmTypeResponses.entrySet().stream()
                .filter(entry -> !FilmType.ofString(entry.getValue().getBody()).isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Long, FilmType> handleFilmIdsToUnknownFilmTypeResponses(Map<Long, ResponseEntity<String>> filmIdsToUnknownFilmTypeResponses, RentalPricingResponse.Builder builder) {
        filmIdsToUnknownFilmTypeResponses.forEach((key, value) -> builder.withMessage("Unknown film type: " + value.getBody() + " for film id: " + key));
        return Collections.emptyMap();
    }

    private Map<Long, FilmType> getFilmIdsToTypes(Map<Long, FilmType> requestFilmIdsToTypes, Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses) {
        Map<Long, FilmType> filmIdsToTypes = requestFilmIdsToTypes == null ? new HashMap<>() : new HashMap<>(requestFilmIdsToTypes);
        filmIdsToTypes.putAll(getFilmIdsToFilmTypesFromResponses(filmIdsToFilmTypeResponses));
        return filmIdsToTypes;
    }

    private Map<Long, FilmType> getFilmIdsToFilmTypesFromResponses(Map<Long, ResponseEntity<String>> filmIdsToFilmTypeResponses) {
        return filmIdsToFilmTypeResponses.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> FilmType.ofString(entry.getValue().getBody()).get()));
    }
}
