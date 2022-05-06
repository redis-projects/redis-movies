package io.redis.configuration;

import com.redis.om.spring.client.RedisModulesClient;
import io.redis.model.Movie;
import io.redisearch.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;

@Slf4j
@Configuration
@ComponentScan
public class RedisConfiguration {

    @Autowired
    private RedisProperties props;

    @Autowired
    RedisModulesClient rmc;

    /**
     * Bootstrap RedisSearch client from the RedisModulesClient
     * This will create a client instance using the index: "io.redis.model.MovieIdx"
     *
     * @param rmc
     * @return
     */
    @Bean
    public Client getRedisSearchClient(RedisModulesClient rmc) {
        String index = Movie.class.getName() + "Idx";
        log.info("Boostrapping RedisSearchClient for Index: {}", index);
        return rmc.clientForSearch(index);
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    @Bean
    public WebClient localApiClient() throws SSLException {
        final int size = 100 * 1024 * 1024;


        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        //.create("https://api.themoviedb.org/3/");
        return WebClient.builder()
                .exchangeStrategies(strategies)
//                .filters(exchangeFilterFunctions -> {
//                    exchangeFilterFunctions.add(logRequest());
//                })
                .baseUrl("https://api.themoviedb.org/3/").build();
    }

//    @Bean
//    JedisConnectionFactory jedisConnectionFactory() {
//        return new JedisConnectionFactory();
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(jedisConnectionFactory());
//        return template;
//    }

}
