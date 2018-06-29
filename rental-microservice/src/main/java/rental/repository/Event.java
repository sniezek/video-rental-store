package rental.repository;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Data
public class Event {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private ZonedDateTime createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EventType type;
    @NotEmpty
    private String value;

    @PrePersist
    void addTimestamp() {
        createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
