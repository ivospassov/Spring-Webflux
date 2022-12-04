package com.example.springwebfluxmongodb.router;

import com.example.springwebfluxmongodb.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder
                            .POST("", reviewHandler::addReview)
                            .GET("", reviewHandler::getAllReviews)
                            .GET("/{id}", reviewHandler::getReviewById)
                            .PUT("/{id}", reviewHandler::updateReview)
                            .DELETE("{id}", reviewHandler::deleteReview)
                            .GET("/stream/flux", reviewHandler::getReviewsStream);
                })
                .build();
    }
}
