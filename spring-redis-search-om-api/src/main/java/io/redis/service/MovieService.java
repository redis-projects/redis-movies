package io.redis.service;

import io.redis.model.Movie;
import io.redis.model.MovieQueryFilter;
import io.redis.repository.MovieRepository;
import io.redis.type.FieldENUM;
import io.redis.type.OperatorENUM;
import io.redis.util.SearchUtil;
import io.redisearch.Client;
import io.redisearch.Query;
import io.redisearch.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class MovieService {

    @Autowired
    MovieRepository repository;

    @Autowired
    SearchUtil searchUtils;

    @Autowired
    @Lazy
    Client redisSearch;

    public Page<Movie> getMoviesByYear(long year, PageRequest request) {
        log.info("Searching for movies by year :: {}", year);

        // Get results
        Instant start = Instant.now();
        Page<Movie> movies = repository.findByYear(year, request);
        Instant finish = Instant.now();
        log.info("Redis 'getMoviesByYear' repository took :: '{}ms' to execute", Duration.between(start, finish).toMillis());

        log.info("Found {} movies, returning the first 20", movies.getTotalElements());
        return movies;
    }

    public Page<Movie> getMoviesBySearch(String query, PageRequest request) {
        log.info("Searching for movies by generic criteria :: '{}'", query);

        // Get results
        Instant start = Instant.now();
        Page<Movie> movies = repository.search(query, request);
        Instant finish = Instant.now();
        log.info("Redis 'getMoviesBySearch' repository took :: '{}ms' to execute", Duration.between(start, finish).toMillis());

        log.info("Found {} movies for search criteria '{}'", movies.getTotalElements(), query);
        return movies;
    }

    /**
     * Search for Movies which exclusively has multiple genres|actors etc.. i.e.
     */
    public Page<Movie> getMoviesByCollection(Set<String> genres, OperatorENUM operator, FieldENUM field, Pageable pageable) {

        if (operator.equals(OperatorENUM.NOT)) {
            log.info("Searching for movies that *DO NOT* contain any of the following genres: '{}'", Arrays.asList(genres));
            return repository.findByGenreNot(genres, pageable);
        } else if (genres.size() == 1 || operator.equals(OperatorENUM.OR)) {
            log.info("Searching for movies that contain any of the following genres: '{}'", Arrays.asList(genres));
            return repository.findByGenre(genres, pageable);
        } else {
            return this.getMoviesByCollectionAND(genres, field.getFieldName(), pageable);
        }
    }

    public Page<Movie> getMoviesByAdvancedSearch(MovieQueryFilter filterQuery, Pageable pageable) {

        // Build the query string
        String query = searchUtils.advancedMovieQueryBuilder(filterQuery);
        log.info("Using Query String: {}", query);

        // Build Query Instance
        Query q = new Query(query);
        q.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

        // Execute Search
        Instant start = Instant.now();
        SearchResult movieSearch = redisSearch.search(q);
        Instant finish = Instant.now();
        log.info("Redis Advanced query took :: '{}ms' to execute", Duration.between(start, finish).toMillis());

        // Collate Results & Return Pageable object
        log.info("Found {} movies", movieSearch.totalResults);
        List<Movie> movies = searchUtils.convertToPageableObject(movieSearch);
        return new PageImpl<>(movies, pageable, movieSearch.totalResults);
    }

    /**
     * Query and ArrayList of values using OR, AND operators and return paginated response
     */
    private Page<Movie> getMoviesByCollectionAND(Set<String> collection, String fieldName, Pageable pageable) {

        // Build Query Instance
        String queryString = searchUtils.buildArrayQuery(collection, fieldName, OperatorENUM.AND);
        log.info("Using Query String: {}", queryString);
        Query query = new Query(queryString);
        query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

        // Execute Search
        Instant start = Instant.now();
        SearchResult movieSearch = redisSearch.search(query);
        Instant finish = Instant.now();
        log.info("Redis 'MoviesByCollection' query took :: '{}ms' to execute", Duration.between(start, finish).toMillis());

        // Collate Results
        log.info("Movie {} search returned '{}' results", fieldName, movieSearch.totalResults);
        List<Movie> movies = searchUtils.convertToPageableObject(movieSearch);
        return new PageImpl<>(movies, pageable, movieSearch.totalResults);
    }
}