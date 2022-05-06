package io.redis.repository;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisDocumentRepository;
import io.redis.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MovieRepository extends RedisDocumentRepository<Movie, String> {

    Page<Movie> findByYear(long year, Pageable pageable);

    Page<Movie> findByYearBetween(int yearGT, int yearLT, Pageable pageable);

    Page<Movie> search(String searchCriteria, Pageable pageable);

    Iterable<Movie> findByGenreAndYear(String genre, int year);

    @Query("@actors:{$actors}")
    Page<Movie> findByActors(@Param("actors") Set<String> actors, Pageable pageable);

    @Query("@genre:{$genre}")
    Page<Movie> findByGenre(@Param("genre") Set<String> genre, Pageable pageable);

    // Negation query i.e. find all movies that != genresList
    @Query("-@genre:{$genre}")
    Page<Movie> findByGenreNot(@Param("genre") Set<String> genre, Pageable pageable);
}
