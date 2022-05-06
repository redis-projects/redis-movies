package io.redis.controller;

import io.redis.model.Movie;
import io.redis.model.MovieQueryFilter;
import io.redis.repository.MovieRepository;
import io.redis.service.MovieService;
import io.redis.type.FieldENUM;
import io.redis.type.OperatorENUM;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("/movie/")
@RestController
public class MovieController {

    @Autowired
    MovieRepository repository;

    @Autowired
    MovieService movieService;

    @GetMapping("/id/{id}")
    public Movie getMovieById(@PathVariable("id") String id) {

        // if only movieId is passed, then change id to key
        if (!StringUtils.containsIgnoreCase(id, Movie.class.getName())) {
            id = Movie.class.getName() + ":" + id;
        }

        return repository.findById(id).get();
    }

    /**
     * Example Search By Actors
     * REST: http://localhost:8080/movie/actors?actors=Chris Evans,Hugo Weaving&page=0&size=20
     * REST (OR): http://localhost:8080/movie/actors?actors=Chris Evans,Robert Downey Jr&page=0&size=20&operator=OR
     * REST (AND): http://localhost:8080/movie/actors?actors=Chris Evans,Scarlett Johansson&page=0&size=20&operator=AND
     * REDIS : "FT.SEARCH" "io.redis.model.MovieIdx" "@actors:{Chris Evans}" "LIMIT" "0" "20"
     * REDIS (OR): "FT.SEARCH" "io.redis.model.MovieIdx" "@actors:{ Hugo Weaving | Chris Evans}" "LIMIT" "0" "20"
     * REDIS (AND): "FT.SEARCH" "io.redis.model.MovieIdx" "@actors:{Chris Evans} @actors:{Scarlett Johansson}" "LIMIT" "0" "20"
     *
     * @param actors
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/actors")
    public Page<Movie> findByActors(@RequestParam(name = "actors") String actors,
                                    @RequestParam(name = "operator", required = false) String operator,
                                    @RequestParam(name = "page", required = false) int page,
                                    @RequestParam(name = "size", required = false) int size) {

        OperatorENUM op = OperatorENUM.fromString(operator) == null
                ? OperatorENUM.AND
                : OperatorENUM.fromString(operator);

        Set<String> cast = Set.of(actors.split(","));
        return movieService.getMoviesByCollection(cast, op, FieldENUM.ACTORS, this.validatePaginationRequest(page, size));
    }

    /**
     * Example Search By Genre
     * REST: http://localhost:8080/movie/genre?genres=Drama&page=0&size=20
     * REST (OR): http://localhost:8080/movie/genre?genres=Acton,Drama&page=0&size=20&operator=OR
     * REST (AND): http://localhost:8080/movie/genre?genres=Action,Adventure&page=0&size=20&operator=AND
     * REST (AND): http://localhost:8080/movie/genre?genres=Action,Adventure&page=0&size=20&operator=NOT
     * REDIS: "FT.SEARCH" "io.redis.model.MovieIdx" "@genre:{ Drama | Acton}" "LIMIT" "0" "20"
     * REDIS (OR): "FT.SEARCH" "io.redis.model.MovieIdx" "@genre:{Action | Adventure}" "LIMIT" "0" "20"
     * REDIS (AND): "FT.SEARCH" "io.redis.model.MovieIdx" "@genre:{Action} @genre:{Adventure} " "LIMIT" "0" "20"
     * REDIS (NOT): "FT.SEARCH" "io.redis.model.MovieIdx" "-@genre:{Action} @genre:{Adventure} " "LIMIT" "0" "20"
     *
     * @param genres
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/genre")
    public Page<Movie> findByGenre(@RequestParam(name = "genres") String genres,
                                   @RequestParam(name = "operator", required = false) String operator,
                                   @RequestParam(name = "page", required = false) int page,
                                   @RequestParam(name = "size", required = false) int size) {

        OperatorENUM op = OperatorENUM.fromString(operator) == null
                ? OperatorENUM.AND
                : OperatorENUM.fromString(operator);

        Set<String> g = Set.of(genres.split(","));

        return movieService.getMoviesByCollection(g, op, FieldENUM.GENRE, this.validatePaginationRequest(page, size));
    }

    /**
     * Example Generic Search
     * REST: http://localhost:8080/movie/search/?page=2&size=20&query=Avengers (Returns 6 results)
     * REST: http://localhost:8080/movie/search/?page=2&size=20&query=The Matrix (Returns 0 results)
     * REST: http://localhost:8080/movie/search/?page=2&size=20&query=Guardians Galaxy (Returns 1 results)
     * REDIS: "FT.SEARCH" "io.redis.model.MovieIdx" "Avengers" "LIMIT" "0" "20"
     * REDIS: "FT.SEARCH" "io.redis.model.MovieIdx" "The Matrix" "LIMIT" "0" "20"
     * REDIS: "FT.SEARCH" "io.redis.model.MovieIdx" "Guardians Galaxy" "LIMIT" "0" "20"
     *
     * @param query
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/search")
    public Page<Movie> search(@RequestParam(name = "query") String query,
                              @RequestParam(name = "page", required = false) int page,
                              @RequestParam(name = "size", required = false) int size) {
        return movieService.getMoviesBySearch(query, this.validatePaginationRequest(page, size));
    }

    /**
     * Example of Delegating request to a service (MovieService)
     * REST : http://localhost:8080/movie/year/2016?page=2&size=20 (Returns 297 results)
     * REDIS: "FT.SEARCH" "io.redis.model.MovieIdx" "@year:[2016 2016]" "LIMIT" "0" "20"
     *
     * @param year
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/year/{year}")
    public Page<Movie> fingByYear(@PathVariable(name = "year") long year,
                                  @RequestParam(name = "page", required = false) int page,
                                  @RequestParam(name = "size", required = false) int size) {
        return movieService.getMoviesByYear(year, this.validatePaginationRequest(page, size));
    }

    /**
     * Example of invoking repository directly
     * REST : http://localhost:8080/movie/years/2014/2019?page=0&size=20 (Returns 522 results)
     * REDIS : "FT.SEARCH" "io.redis.model.MovieIdx" "@year:[2014 2019]" "LIMIT" "0" "20"
     *
     * @param gt
     * @param lt
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/years/{gt}/{lt}")
    public Page<Movie> findByYearBetween(@PathVariable("gt") int gt, @PathVariable("lt") int lt,
                                         @RequestParam(name = "page", required = false) int page,
                                         @RequestParam(name = "size", required = false) int size) {
        return repository.findByYearBetween(gt, lt, this.validatePaginationRequest(page, size));
    }

    /**
     * REST : http://localhost:8080/movie/advanced/search?page=0&size=20' \
     * --data-raw '{
     *     "genericCriteria" : "Avengers",
     *     "actors" : "Chris Evans,Scarlett Johansson",
     *     "actorOperator": "AND",
     *     "genres" : "Action,Sci-Fi",
     *     "genreOperator": "OR",
     *     "directors" : "Joss Whedon, Anthony Russo",
     *     "releaseYearGTE" : 2005,
     *     "imdbRatingGTE" : 9,
     *     "imdbRatingLTE" : 7,
     *     "metaRatingGTE" : 70,
     *     "runtimeLTE" : 160
     * }'
     * (Returns 2 results)
     * REDIS : "Avengers @actors:{Chris Evans} @actors:{Scarlett Johansson} @director:(Joss Whedon|Anthony Russo)
     *          @genre:{Action|Sci\\-Fi} @rating:[7 9] @year:[2005 inf]" "LIMIT" "0" "20"
     * @param movieQueryFilter
     * @param page
     * @param size
     * @return
     */
    @PostMapping("/advanced/search")
    public Page<Movie> advancedSearch(@RequestBody MovieQueryFilter movieQueryFilter,
                                      @RequestParam(name = "page", required = false) int page,
                                      @RequestParam(name = "size", required = false) int size) {
        return movieService.getMoviesByAdvancedSearch(movieQueryFilter, this.validatePaginationRequest(page, size));
    }

    private PageRequest validatePaginationRequest(int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        return PageRequest.of(page, pageSize);
    }
}
