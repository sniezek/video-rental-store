package rental.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FilmType {
    NEW(2),
    REGULAR(1),
    OLD(1);

    @Getter
    private final int customerBonusPointsForRental;
}
