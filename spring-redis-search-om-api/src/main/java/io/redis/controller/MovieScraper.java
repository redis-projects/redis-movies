package io.redis.controller;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.redis.service.MovieScraperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Slf4j
@CrossOrigin(origins = "*")
@RequestMapping("/scraper")
@RestController
public class MovieScraper {

    private static FileWriter fileWriter;

    @Autowired
    MovieScraperService movieScraperService;

    @Autowired
    Gson gson;

    @RequestMapping(value = "page/{page}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMoviesByPage(@PathVariable("page") Integer page) {
        return movieScraperService.getMovies(page);
    }

//    @RequestMapping(value = "page/{page}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    public String getMoviesByPage(@PathVariable("page") Integer page) {
//        return movieScraperService.getMovies(page);
//    }

    /**
     * Scrape Movies from TMDB
     * REST : http://localhost:8080/scraper/bulk/500
     * Scrape the first 500 pages from the TMDB website. D:
     * @throws IOException
     */
    @RequestMapping(value = "bulk/{movies}", method = RequestMethod.GET )
    public void getMoviesBulk(@PathVariable("movies") Integer movies) throws IOException {

        AtomicBoolean bail = new AtomicBoolean(false);
        int movieCount = 0;
        int page=1;
        JsonArray movieArray = new JsonArray();

        while (movies > movieCount){
            log.info("Obtaining page: {}", page);

            String response;
            try {
                response = movieScraperService.getMovies(page);
            } catch (Exception e) {
                log.error("API Call failed on page {}", page);
                // break out of array
                bail.set(true);
                return;
            }

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray moviesPage = json.getAsJsonArray("results");
            movieCount += moviesPage.size();
            log.info("Adding {} movies, total: {}", moviesPage.size(), movieCount);
            movieArray.addAll(moviesPage);
            page++;
        }

        if (!bail.get()) {
            this.writeToFile(movieArray, "new-movies.json");
        }
    }

    /**
     * Write file to disk
     */
    private void writeToFile(JsonArray jsonArray, String filename) throws IOException {
        String path = "src/main/resources/movies" + File.separator + filename;
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();

        fileWriter = new FileWriter(file);
        fileWriter.write(jsonArray.toString());
        fileWriter.flush();
        log.info("finished writing {} to disk", filename);
    }

}
