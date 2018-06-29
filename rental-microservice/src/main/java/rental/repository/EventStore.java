package rental.repository;

import org.springframework.data.repository.CrudRepository;

public interface EventStore extends CrudRepository<Event, Long> {
}
