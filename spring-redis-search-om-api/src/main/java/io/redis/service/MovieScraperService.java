package io.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class MovieScraperService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private final WebClient tmdbApiClient;

    @Autowired
    public MovieScraperService(WebClient tmdbApiClient) {
        this.tmdbApiClient = tmdbApiClient;
    }

    public String getMovies(int page) {
        return tmdbApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("movie/top_rated")
                        .queryParam("page", page)
                        .queryParam("api_key", "8be8e5bc13a1edef7fd11cedcdce8a27")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block(REQUEST_TIMEOUT);
    }
    //                        .queryParam("sort_by", "vote_average.desc")
//                        .queryParam("include_adult", "false")
//                        .queryParam("vote_count.gte", 200)

    public String getMovieById(long id) {
        return tmdbApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("movie/" + id)
                        .queryParam("append_to_response", "credits")
                        .queryParam("api_key", "8be8e5bc13a1edef7fd11cedcdce8a27")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block(REQUEST_TIMEOUT);
    }
}
