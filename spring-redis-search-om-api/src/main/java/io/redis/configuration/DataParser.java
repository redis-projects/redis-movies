package io.redis.configuration;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.redis.service.MovieScraperService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class DataParser implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    MovieScraperService movieScraperService;

    //private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static FileWriter file;

    // JSON Directory
    private static final String FILE_DIR = "src/main/resources/json/";
    private static final String MOVIE_DIR = "src/main/resources/movies/";

    // JSON Fields
    private static final String RANK = "Rank";
    private static final String TITLE = "Title";
    private static final String GENRE = "Genre";
    private static final String GENRES = "Genre";
    private static final String DESCRIPTION = "Description";
    private static final String DIRECTOR = "Director";
    private static final String ACTORS = "Actors";
    private static final String CAST = "Actors";
    private static final String YEAR = "Year";
    private static final String RUNTIME = "Runtime (Minutes)";
    private static final String RATING = "Rating";
    private static final String VOTES = "Votes";
    private static final String REVENUE = "Revenue (Millions)";
    private static final String METASCORE = "Metascore";

    @Value("${spring.redis.movie.data.parse.raw-json:false}")
    private boolean parse;

    /**
     * Iterate over existing file and convert
     *
     * @throws IOException
     */
    private void readData() throws IOException {

        // Get all JSON Files
        JsonReader reader = new JsonReader(new FileReader(FILE_DIR + "legacy/movies.json"));
        JsonArray moviesJSON = JsonParser.parseReader(reader).getAsJsonArray();

        JsonArray restructuredJSON = new JsonArray();
        Iterator<JsonElement> movieIT = moviesJSON.iterator();
        while (movieIT.hasNext()) {
            JsonObject movie = movieIT.next().getAsJsonObject();

            String genre = movie.get(GENRE).getAsString();
            String actors = movie.get(ACTORS).getAsString();

            List<String> genres = Arrays.asList(genre.split("\\s*,\\s*"));
            List<String> cast = Arrays.asList(actors.split("\\s*,\\s*"));

            movie.add(GENRES, gson.toJsonTree(genres));
            movie.add(CAST, gson.toJsonTree(cast));

            restructuredJSON.add(movie);
        }

        this.writeToFile(restructuredJSON, "legacy/movies.json");

    }

    private void getFileDetails() throws IOException {

        JsonReader readerNew = new JsonReader(new FileReader("src/main/resources/parsed-movies.json"));
        JsonArray moviesNew = JsonParser.parseReader(readerNew).getAsJsonArray();
        Iterator<JsonElement> movieIT = moviesNew.iterator();
        StringBuilder movies = new StringBuilder();
        while (movieIT.hasNext()) {
            String movie = movieIT.next().getAsJsonObject().get("title").getAsString();
            log.info("Movie: {}", movie);
            movies.append(movie);
            movies.append("\n");
        }

        this.writeToFileString(movies.toString(), "movie-titles.json");
        log.info("Movies New: {}", moviesNew.size());
    }


    private void readTMDBData() throws IOException {

        // Get all JSON Files
        JsonReader reader = new JsonReader(new FileReader(MOVIE_DIR + "new-movies.json"));
        JsonArray moviesJSON = JsonParser.parseReader(reader).getAsJsonArray();

        JsonArray restructuredJSON = new JsonArray();
        Set<String> genres = new HashSet<>();
        Iterator<JsonElement> movieIT = moviesJSON.iterator();
        while (movieIT.hasNext()) {
            JsonObject movie = movieIT.next().getAsJsonObject();
            long id = movie.get("id").getAsLong();

            log.info("Getting Movie with id: {}", id);
            String movieDetails = movieScraperService.getMovieById(id);

            if (StringUtils.isNotBlank(movieDetails)) {
                JsonObject md = JsonParser.parseString(movieDetails).getAsJsonObject();
                movie.add("budget", md.get("budget"));
                movie.add("imdb_id", md.get("imdb_id"));
                movie.add("revenue", md.get("revenue"));
                movie.add("runtime", md.get("runtime"));
                movie.add("status", md.get("status"));
                movie.add("tagline", md.get("tagline"));
                movie.add("genres", this.getGenres(genres, md.getAsJsonArray("genres")));

                JsonObject credits = md.getAsJsonObject("credits");
                movie.add("cast", this.getCast(credits.getAsJsonArray("cast"), "actor"));
                movie.add("director", this.getCast(credits.getAsJsonArray("crew"), "director"));
                movie.add("writer", this.getCast(credits.getAsJsonArray("crew"), "writer"));

                movie.remove("genre_ids");
                movie.remove("backdrop_path");
                movie.remove("original_title");
                movie.remove("adult");
                movie.remove("video");
            }

            restructuredJSON.add(movie);
        }

        this.writeToFileString(gson.toJson(genres), "genres.json");
        this.writeToFile(restructuredJSON, "parsed-movies.json");
    }

    private JsonArray getGenres(Set<String> genreSet, JsonArray genres) {
        JsonArray array = new JsonArray();
        Iterator<JsonElement> genreIT = genres.iterator();

        while (genreIT.hasNext()) {
            JsonObject g = genreIT.next().getAsJsonObject();
            array.add(g.get("name").getAsString());
            genreSet.add(g.get("name").getAsString());
        }
        return array;
    }

    private JsonArray getCast(JsonArray array, String identifier) {
        List<String> writerIdentifiers = new ArrayList<>(List.of("Screenplay", "Writer"));
        JsonArray castCrew = new JsonArray();
        Iterator<JsonElement> castIT = array.iterator();
        int count = 0;

        while (castIT.hasNext()) {
            JsonObject g = castIT.next().getAsJsonObject();
            JsonObject cm = new JsonObject();

            if (count >= 10) {
                break;
            }

            if (identifier.equalsIgnoreCase("director")) {
                if (!g.get("job").getAsString().equalsIgnoreCase("Director")) {
                    continue;
                } else {
                    cm.add("job", g.get("job"));
                }
            }

            if (identifier.equalsIgnoreCase("writer")) {
                if (!containsCaseInsensitive(g.get("job").getAsString(), writerIdentifiers)) {
                    continue;
                } else {
                    cm.add("job", g.get("job"));
                }
            }

            if (identifier.equalsIgnoreCase("actor")) {
                count++;
                cm.add("character", g.get("character"));
            }

            int genderId = g.get("gender").getAsInt();
            String gender = "Unknown";
            if (genderId > 0) {
                gender = genderId == 1 ? "Female" : "Male";
            }

            cm.addProperty("gender", gender);
            cm.add("id", g.get("id"));
            cm.add("name", g.get("name"));
            cm.add("profile_path", g.get("profile_path"));
            castCrew.add(cm);
        }
        return castCrew;
    }

    public boolean containsCaseInsensitive(String s, List<String> l) {
        return l.stream().anyMatch(x -> x.equalsIgnoreCase(s));
    }

    private void writeToFile(JsonElement json, String filename) throws IOException {
        file = new FileWriter("src/main/resources/" + filename);
        file.write(gson.toJson(json));
        file.flush();
        log.info("finished writing {} to disk", filename);
    }

    private void writeToFileString(String json, String filename) throws IOException {
        file = new FileWriter("src/main/resources/" + filename);
        file.write(json);
        file.flush();
        log.info("finished writing {} to disk", filename);
    }


    /**
     * Seed Database with JSON file in resources folder.
     *
     * @param event
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //if (parse) {
            // Parse JSON files to produce restructured JSON
            log.info("Parsing movies JSON files in /src/main/resources/movies");
            // this.readTMDBData();
            //this.getFileDetails();
       // }
    }
}
