package io.redis;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = "io.redis.*")
public class SpringRedisMovieSearch {

    public static void main(String[] args) {
        SpringApplication.run(SpringRedisMovieSearch.class, args);
    }

}
