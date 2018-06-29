package film.service;

import film.domain.Film;
import film.repository.FilmRepository;
import film.repository.rental.RentalRepository;
import film.repository.rental.record.rental.RentalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmCountService {
    private final FilmRepository filmRepository;
    private final RentalRepository rentalRepository;

    public Optional<Integer> getFilmCount(Long filmId) {
        return filmRepository.findById(filmId)
                .map(Film::getOverallCount)
                .map(overallCount -> getCurrentCount(overallCount, filmId));
    }

    private int getCurrentCount(int overallCount, long filmId) {
        int overallRented = rentalRepository.getRentals(filmId).stream()
                .map(RentalRecord::getRentals)
                .flatMap(Collection::stream)
                .filter(ren -> ren.getFilmId() == filmId)
                .mapToInt(RentalRecord.FilmRental::getFilmCount)
                .sum();

        int overallReturned = rentalRepository.getReturns(filmId).stream()
                .mapToInt(ret -> ret.getFilmIdsToCounts().get(filmId))
                .sum();

        return overallCount - overallRented + overallReturned;
    }
}
