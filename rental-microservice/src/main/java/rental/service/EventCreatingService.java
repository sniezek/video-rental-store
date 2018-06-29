package rental.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import rental.repository.Event;
import rental.repository.EventStore;
import rental.repository.EventType;
import rental.repository.EventValue;

abstract class EventCreatingService<RQ> {
    private final EventStore eventStore;
    private final ObjectMapper objectMapper;

    EventCreatingService(EventStore eventStore, ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.objectMapper = objectMapper;
    }

    public abstract void apply(RQ request);

    void createEvent(EventValue value) {
        try {
            Event event = new Event();
            event.setType(EventType.fromEventValueClass(value.getClass()));
            event.setValue(objectMapper.writeValueAsString(value));
            eventStore.save(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
