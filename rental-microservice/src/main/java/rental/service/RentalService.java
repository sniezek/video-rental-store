package rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import rental.domain.event.AddedCustomerBonusPoints;
import rental.domain.event.Rental;
import rental.repository.EventStore;

import java.util.HashMap;
import java.util.Map;

@Service
public class RentalService extends EventCreatingService<Rental> {
    private final TransactionTemplate transactionTemplate;

    RentalService(EventStore eventStore, ObjectMapper objectMapper, TransactionTemplate transactionTemplate) {
        super(eventStore, objectMapper);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void apply(Rental request) {
        transactionTemplate.execute(status -> {
            createRentalEvent(request);
            createCustomerBonusPointsEvent(request);
            return null;
        });
    }

    private void createRentalEvent(Rental request) {
        createEvent(request);
    }

    private void createCustomerBonusPointsEvent(Rental request) {
        createEvent(new AddedCustomerBonusPoints(request.getCustomerId(), calculateCustomerBonusPoints(request)));
    }

    int calculateCustomerBonusPoints(Rental request) {
        Map<Long, Integer> filmIdsToCounts = new HashMap<>();

        for (Rental.FilmRental rental : request.getRentals()) {
            filmIdsToCounts.merge(rental.getFilmId(), rental.getFilmCount(), (prev, curr) -> prev + curr);
        }

        return filmIdsToCounts.entrySet().stream()
                .mapToInt(e -> request.getFilmIdsToTypes().get(e.getKey()).getCustomerBonusPointsForRental() * e.getValue())
                .sum();
    }
}
