package film.repository;

import film.domain.Film;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilmRepository extends PagingAndSortingRepository<Film, Long> {
    List<Film> findByNameStartsWith(@Param("name") String name);
}
