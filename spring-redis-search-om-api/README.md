# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.6/maven-plugin/reference/html/#build-image)
* [Spring Data Redis (Access+Driver)](https://docs.spring.io/spring-boot/docs/2.5.6/reference/htmlsingle/#boot-features-redis)

### Guides

---
**NOTE**: I am currently refactoring this codebase for more complex JSON examples i.e. nested objectsm larger data sets and increased query complexity.
Regardless, the new implementation will only replace the current impl when finished. 

----

This example demonstrates Redis Search and Redis JSON over a collection of movies. 
This project utilises Spring Data and Redis OM to demonstrate the current developer experience. 

Redis Search categorises data into 3 specific types (https://redis.io/docs/stack/search/reference/query_syntax/): 
- TEXT: Free text search across specific or multiple fields 
- NUMERIC: Number based queries including dates, geo, vector similarity etc.. 
- TAG: Tokenised fields such as Categories, Genres, Cities etc.. 

#### Redis Queries 

The following query types are used in this demo, this is an expanding list: 

Generic Text Search Terms:
* Generic Search i.e. search all (text) fields for phrase: `FT.SEARCH "io.redis.model.MovieIdx" "Shut"`
* Search By Specific field i.e. Title: `FT.SEARCH "io.redis.model.MovieIdx" "@title:Shut"`
  * AND:  `"@title:Shut In" (Search for Shut+In)"`
  * EXACT: `"@title:Shut In"`
  * FUZZY: `"@title:'Shut*'"`
  * WILDCARD: `"@title:'%Shut%'"`
  * EXCLUSION/NEGATION: "@title:'Shut -Out'"
        * Search for 'Shut' but only show titles that DO NOT container 'Out'

Tag Search Terms:
* Search By Tags in a Collection:
* Search By Genre tag: `FT.SEARCH "io.redis.model.MovieIdx" "@genre:{Drama}"`
  * Find all movies with the genre `Drama`
* Search By Genre (fuzzy): `FT.SEARCH "io.redis.model.MovieIdx" "@genre:{ dram* }"`
  * Find all movies with the (fuzzy) genre `Dram*`
* Search By Multiple Values (OR): `"FT.SEARCH" "io.redis.model.MovieIdx" "@genre:{ Drama | Acton}"`
  * Find all movies with the genre `Drama` OR `Action`
* Search By Multiple Values (AND): `"FT.SEARCH" "io.redis.model.MovieIdx" "@actors:{Chris Evans} @actors:{Scarlett Johansson}"`
  * Find all movies with the actors `Chris Evans` AND `Scarlett Johansson`
* Search By Multiple Values (NOT): `"FT.SEARCH" "io.redis.model.MovieIdx" "-@genres:{Action|Adventure}"`
  * Find all movies with that DO NOT have the genre `Action` OR `Adventure`
* Multi Terms : `"@genre:{Drama} @genre:{Horror} @actors{Naomi Watts}"`
  * Find all movies with the multiple criteria specified. 

Numeric Search Terms:
* Search By Rating Range: `FT.SEARCH "io.redis.model.MovieIdx" "@rating:[4 6]"`
  * Greater than: `"@rating:[(4 inf]"`
  * Greater than or Equal: `"@rating:[4 inf]"`
  * Less than: `"@rating:[-inf (5]"`
  * Less than or Equal: `"@rating:[-inf (5]"`
  * Specific year: `"@rating:[(2014]"`
  * Ranges: `"@year:[2014 2019]"`
    * i.e. find all movies between (GTE) 2014 and 2019 (LTE)
      * GTE - Greater Than or Equal To
      * LTE - Less Than or Equal To

---
**NOTE**: Good practice would suggest storing dates as UTC, making querying simpler and date conversions consistent etc..

----

#### Run Redis locally

1. We're going to be using Redis Stack: https://redis.io/docs/stack/

This gives you the option to run a Redis instance either as a local binary or docker container. 
In this guide we'll run it as a docker container: 

https://redis.io/docs/stack/get-started/install/docker/
```bash 
$ docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
fa1df7ee6788b754668ce4e19d1d3aae1040ac06d1ca03798863242d068f13c7

or to resume a previous session
$ docker start redis-stack 
redis-stack

```
---
**NOTE**: Redis Insights is incorporated in Redis-Stack. Navigate to http://localhost:8001/redis-stack

----

* **TIP**: When running this demo I highly recommend connecting to the Redis instance via the CLI and using the `monitor` command
    * `[monitor](https://redis.io/commands/monitor/)` will echo all Redis requests send to the server, so you can clearly see the syntax being used  

2. To connect to the redis cli, simply provide the port number: 
```bash
$ redis-cli -p 6379
127.0.0.1:6379> dbsize
(integer) 0
127.0.0.1:6379> set hello "world"
OK
127.0.0.1:6379> dbsize
(integer) 1
```

3. Build and package the Spring Boot app, there are currently no tests 😬 😬 😬 😬

```bash
$ mvn clean pacakge -DskipTests
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.724 s
[INFO] Finished at: 2022-05-06T17:51:34+01:00
[INFO] ------------------------------------------------------------------------
```

4. Run the Spring Boot Instance! 

The bootup sequence will inform you of all the various Queries and Beans that have been found and the corresponding Indexes etc.. that have been created. 

```bash 
$ mvn clean spring-boot:run 
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.6.7)

2022-05-06 18:05:20.371  INFO 80472 --- [           main] io.redis.SpringRedisMovieSearch          : Starting SpringRedisMovieSearch using Java 18 on alistair.jarrett-C02G84UVML7J with PID 80472 (/Users/ajarrett/Documents/Projects/Java/json-search-om/spring-redis-search-om-api/target/classes started by ajarrett in /Users/ajarrett/Documents/Projects/Java/json-search-om/spring-redis-search-om-api)
2022-05-06 18:05:20.373  INFO 80472 --- [           main] io.redis.SpringRedisMovieSearch          : No active profile set, falling back to 1 default profile: "default"
2022-05-06 18:05:20.970  INFO 80472 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Multiple Spring Data modules found, entering strict repository configuration mode!
2022-05-06 18:05:20.971  INFO 80472 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data RedisJSON repositories in DEFAULT mode.
2022-05-06 18:05:21.126  INFO 80472 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 140 ms. Found 1 RedisJSON repository interfaces.
2022-05-06 18:05:21.875  INFO 80472 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2022-05-06 18:05:21.885  INFO 80472 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-05-06 18:05:21.886  INFO 80472 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.62]
2022-05-06 18:05:21.991  INFO 80472 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-05-06 18:05:21.991  INFO 80472 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1555 ms
2022-05-06 18:05:22.374  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating search query method
2022-05-06 18:05:22.379  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByYearBetween query method
2022-05-06 18:05:22.389  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByGenreNot query method
2022-05-06 18:05:22.390  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByGenre query method
2022-05-06 18:05:22.390  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByYear query method
2022-05-06 18:05:22.391  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByGenreAndYear query method
2022-05-06 18:05:22.392  INFO 80472 --- [           main] c.r.o.s.r.query.RediSearchQuery          : Creating findByActors query method
2022-05-06 18:05:23.002  INFO 80472 --- [           main] i.r.configuration.RedisConfiguration     : Boostrapping RedisSearchClient for Index: io.redis.model.MovieIdx
2022-05-06 18:05:23.296  INFO 80472 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-05-06 18:05:23.584  INFO 80472 --- [           main] io.redis.configuration.DataLoader        : Loading 1000 movies into Redis
2022-05-06 18:05:31.125  INFO 80472 --- [           main] io.redis.configuration.DataLoader        : Finished loading data into Redis
2022-05-06 18:05:31.125  INFO 80472 --- [           main] io.redis.configuration.DataParser        : Parsing movies JSON files in /src/main/resources/movies
2022-05-06 18:05:31.126  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : Creating Indexes......
2022-05-06 18:05:31.135  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : Found 1 @Document annotated Beans...
2022-05-06 18:05:31.135  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : Found @Document annotated class: io.redis.model.Movie
2022-05-06 18:05:31.135  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : FOUND @Searchable annotation on field of type: class java.lang.String
2022-05-06 18:05:31.138  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : FOUND @Searchable annotation on field of type: class java.lang.String
2022-05-06 18:05:31.139  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : FOUND @Searchable annotation on field of type: class java.lang.String
2022-05-06 18:05:31.143  WARN 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : Skipping index creation for io.redis.model.MovieIdx because Index already exists
2022-05-06 18:05:31.151  INFO 80472 --- [           main] c.r.om.spring.RedisModulesConfiguration  : Found 0 @RedisHash annotated Beans...
2022-05-06 18:05:31.163  INFO 80472 --- [           main] io.redis.SpringRedisMovieSearch          : Started SpringRedisMovieSearch in 11.173 seconds (JVM running for 11.521)

```

----
**NOTE**: This Spring Boot instance assumes you've started Redis Stack on host:`localhost` and port:`6379` please update the application.properties if this is not the case. 
* Or override the following env: 
  * `spring.redis.host=localhost`
  * `spring.redis.port=6379`

---

In addition, the above, the app will automatically insert 1000 movies from the ./src/main/resources/legacy/movies.json file
This is triggered by the prop: `spring.redis.movie.data.insert-on-startup=true`

5. Check Redis Insights 

* You should see 1000 records have been created in the explorer with the key prefix: `io.redis.model.Movie` 
  * `io.redis.model.Movie` is the package + class name of our Redis OM class appended with a generated ID   

6. Check Out the Movie Controller 

* There are a bunch of REST endpoints that can be used to execute the implemented MovieRepository
  * The Controller also container lots of CURL requests, which you can paste into something like PostMan e.g. 

e.g. 

```java
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
        
        // Default to 'AND' if the designated Operator is missing
        OperatorENUM op = OperatorENUM.fromString(operator) == null
                ? OperatorENUM.AND
                : OperatorENUM.fromString(operator);

        Set<String> g = Set.of(genres.split(","));

        return movieService.getMoviesByCollection(g, op, FieldENUM.GENRE, this.validatePaginationRequest(page, size));
    }
```

### Useful Commands: 

* List the index created:
    ```bash
    127.0.0.1:6379> FT._LIST
    1) "io.redis.model.MovieIdx"
    ```

* View Index details:
    ```bash
    127.0.0.1:6379> FT.INFO io.redis.model.MovieIdx
    ...<LOTS OF DETAIL>...
    ```

* Delete Index: 
    ```bash
     FT.DROPINDEX io.redis.model.MovieIdx
    ```

  * Add JSON data :
      ```bash
       "JSON.SET" "io.redis.model.Movie:1" "." 
          "{
              \"movieId\":\"1\",
              \"rank\":827,
              \"title\":\"Shut In\",
              \"genre\":[\"Drama\",\"Horror\",\"Thriller\"],
              \"description\":\"A heart-pounding thriller about a widowed child psychologist who lives in an isolated existence in rural New England. Caught in a deadly winter storm, she must find a way to rescue a young boy before he disappears forever.\",
              \"director\":\"Farren Blackburn\",
              \"actors\":[\"Naomi Watts\",\"Charlie Heaton\",\"Jacob Tremblay\",\"Oliver Platt\"],
              \"year\":2016,
              \"runtime\":91,
              \"rating\":4.6,
              \"votes\":5715,
              \"revenue\":6.88,
              \"metascore\":25
          }"
      ```

* Create the corresponding index
    ```bash
    "FT.CREATE" "io.redis.model.MovieIdx" "ON" "JSON" "PREFIX" "1" "io.redis.model.Movie:" 
       "SCHEMA" "$.title" "AS" "title" "TEXT" "WEIGHT" "3.0" 
                "$.genre[*]" "AS" "genre" "TAG" 
                "$.description" "AS" "description" "TEXT" 
                "$.director" "AS" "director" "TEXT" "WEIGHT" "3.0" 
                "$.actors[*]" "AS" "actors" "TAG" 
                "$.year" "AS" "year" "NUMERIC" 
                "$.runtime" "AS" "runtime" "NUMERIC" 
                "$.rating" "AS" "rating" "NUMERIC" 
                "$.votes" "AS" "votes" "NUMERIC" 
                "$.revenue" "AS" "revenue" "NUMERIC" 
                "$.metascore" "AS" "metascore" "NUMERIC"`
    ```