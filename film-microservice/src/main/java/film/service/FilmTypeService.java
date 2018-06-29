package film.service;

import film.domain.Film;
import film.domain.FilmType;
import film.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmTypeService {
    private final FilmRepository filmRepository;

    public Optional<FilmType> getFilmType(Long filmId) {
        return filmRepository.findById(filmId)
                .map(Film::getPremiere)
                .map(FilmType::fromPremiere);
    }
}
