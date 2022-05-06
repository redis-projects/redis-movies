package io.redis.configuration;

import com.google.gson.Gson;
import io.redis.model.Movie;
import io.redis.repository.MovieRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
//@DependsOn("redisConfiguration")
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    MovieRepository movieRepository;


    @Value("${spring.redis.movie.data.insert-on-startup:false}")
    private boolean load;

    @Value("${spring.redis.movie.data.delete-on-shutdown:false}")
    private boolean deleteOnShutdown;

    private static FileWriter file;

    // JSON Directory
    private static final String FILE_DIR = "src/main/resources/";
    private static final String SAMPLE_FILE = "legacy/movies.json";

    /**
     * Seed Database with JSON file in resources folder.
     *
     * @param event
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (load) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_DIR + SAMPLE_FILE));

            // Serialise to Movie Array from disk
            List<Movie> movies = Arrays.asList(new Gson().fromJson(bufferedReader, Movie[].class));
            log.info("Loading {} movies into Redis", movies.size());

            // Batch load into Redis
            movieRepository.saveAll(movies);
            log.info("Finished loading data into Redis");
        }
    }

    /**
     * Delete all data
     */
    @PreDestroy
    public void destroy() {
        if (deleteOnShutdown) {
            log.info("Deleting json data from Redis");
            movieRepository.deleteAll();
        }
    }
}
