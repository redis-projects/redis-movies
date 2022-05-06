package io.redis.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private int database = 0;

    @Value("${host:localhost}")
    private String host = "localhost";

    @Value("${port:6379}")
    private int port;

    @Value("${graph.name:my-graph}")
    private String graphname;

    @Value("${username:username}")
    private String username;

    @Value("${password:#{null}}")
    private String password;

//    private boolean ssl;
//    private Duration timeout;
//    private Duration connectTimeout;
//    private String clientName;

}
