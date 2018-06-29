package film.controller;

import film.service.FilmCountService;
import film.service.FilmTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/films")
@RequiredArgsConstructor
// Spring Data REST endpoints also active
public class FilmController {
    private static final ResponseEntity<String> FILM_NOT_FOUND = new ResponseEntity<>("Film not found.", HttpStatus.NOT_FOUND);

    private final FilmTypeService filmTypeService;
    private final FilmCountService filmCountService;

    @GetMapping("/{filmId}/type")
    public ResponseEntity<String> getFilmType(@PathVariable Long filmId) {
        return filmTypeService.getFilmType(filmId)
                .map(Enum::toString)
                .map(filmType -> new ResponseEntity<>(filmType, HttpStatus.OK))
                .orElse(FILM_NOT_FOUND);
    }

    @GetMapping("/{filmId}/count")
    public ResponseEntity<String> getFilmCount(@PathVariable Long filmId) {
        return filmCountService.getFilmCount(filmId)
                .map(Long::toString)
                .map(count -> new ResponseEntity<>(count, HttpStatus.OK))
                .orElse(FILM_NOT_FOUND);
    }
}
