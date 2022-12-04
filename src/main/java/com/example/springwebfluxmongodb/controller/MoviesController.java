package com.example.springwebfluxmongodb.controller;

import com.example.springwebfluxmongodb.client.MoviesInfoRestClient;
import com.example.springwebfluxmongodb.client.ReviewsRestClient;
import com.example.springwebfluxmongodb.domain.Movie;
import com.example.springwebfluxmongodb.domain.Review;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

//TODO
@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MoviesInfoRestClient moviesInfoRestClient;
    private ReviewsRestClient reviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieInfoById(@PathVariable("id") String movieId) {
        moviesInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsListMono = reviewsRestClient
                            .retrieveReviews(movieInfo.getMovieInfoId()).collectList();

                    return reviewsListMono.map(
                            reviews -> new Movie(movieInfo, reviews));

                });
        return null;
    }
}
