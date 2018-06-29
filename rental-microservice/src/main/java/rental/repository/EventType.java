package rental.repository;

import lombok.RequiredArgsConstructor;
import rental.domain.event.AddedCustomerBonusPoints;
import rental.domain.event.Rental;
import rental.domain.event.Return;

@RequiredArgsConstructor
public enum EventType {
    RENTAL(Rental.class),
    RETURN(Return.class),
    BONUS_POINTS(AddedCustomerBonusPoints.class);

    private final Class<? extends EventValue> eventValueClass;

    public static EventType fromEventValueClass(Class<? extends EventValue> clazz) {
        for (EventType type : values()) {
            if (type.eventValueClass.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No EventType defined for EventValue class: " + clazz.toString());
    }
}
