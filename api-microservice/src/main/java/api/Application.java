package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class Application {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/films/**").uri("http://localhost:8080"))
                .route(r -> r.path("/price-calculations/rentals").uri("http://localhost:8081/rentals"))
                .route(r -> r.path("/price-calculations/returns").uri("http://localhost:8081/returns"))
                .route(r -> r.path("/rentals").uri("http://localhost:8082/rentals"))
                .route(r -> r.path("/returns").uri("http://localhost:8082/returns"))
                .route(r -> r.path("/customers/**").uri("http://localhost:8083"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
