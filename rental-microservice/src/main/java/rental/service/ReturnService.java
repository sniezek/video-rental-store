package rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import rental.domain.event.Return;
import rental.repository.EventStore;

@Service
public class ReturnService extends EventCreatingService<Return> {
    ReturnService(EventStore eventStore, ObjectMapper objectMapper) {
        super(eventStore, objectMapper);
    }

    @Override
    public void apply(Return request) {
        createEvent(request);
    }
}
