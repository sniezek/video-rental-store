package film.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public enum FilmType {
    NEW(0),
    REGULAR(30),
    OLD(3650);

    private static final List<FilmType> OLDEST_TO_NEWEST =
            Stream.of(values())
                    .sorted(comparingInt(FilmType::getMinimumDaysAfterPremiere).reversed())
                    .collect(toList());

    @Getter
    private final int minimumDaysAfterPremiere;

    public static FilmType fromPremiere(LocalDate premiereDate) {
        long daysOld = DAYS.between(premiereDate, LocalDate.now());

        return OLDEST_TO_NEWEST.stream()
                .filter(filmType -> filmType.getMinimumDaysAfterPremiere() <= daysOld)
                .findFirst()
                .orElse(NEW); // for films which have not had a premiere yet
    }
}
