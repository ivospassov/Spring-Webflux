package com.example.springwebfluxmongodb.router;

import com.example.springwebfluxmongodb.domain.Review;
import com.example.springwebfluxmongodb.exceptionHandler.GlobalErrorHandler;
import com.example.springwebfluxmongodb.exceptions.ReviewNotFoundException;
import com.example.springwebfluxmongodb.handler.ReviewHandler;
import com.example.springwebfluxmongodb.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
class ReviewsUnitTest {

    @MockBean
    ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    WebTestClient webTestClient;

    static String REVIEW_URI = "/v1/reviews";

    @Test
    void addReview() {
        Review newReview = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .post()
                .uri(REVIEW_URI)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();

                    assert responseBody != null;
                    assert responseBody.getReviewId() != null;
                });
    }

    @Test
    void addReviewValidation() {
        Review newReview = new Review("abc", null, "Awesome Movie", -9.0);

        when(this.reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome movie", 9.0)));

        webTestClient
                .post()
                .uri(REVIEW_URI)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(error -> {
                    String errorResponseBody = error.getResponseBody();

                    assert errorResponseBody != null;
                    assertEquals(errorResponseBody, "movieInfoId : must not be null, rating.negative : please pass a non-negative value");
                });
    }

    @Test
    void getAllReviews() {

        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 2L, "Nice Movie 2", 8.0),
                new Review(null, 1L, "Cool Movie 3", 7.0));

        when(this.reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

        webTestClient
                .get()
                .uri(REVIEW_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getReviewsByMovieInfoId() {

        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Cool Movie 3", 7.0));

        when(this.reviewReactiveRepository.findByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(reviews));

        URI uriWithQueryParam = UriComponentsBuilder.fromUriString(REVIEW_URI).queryParam("movieInfoId", 1).buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uriWithQueryParam)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReviewByIdNotFoundException() {

        Review existingReview = new Review("correctId", 1L, "Awesome Movie", 9.0);

        when(this.reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.error(new ReviewNotFoundException("Review not found for the given Review id")));

        webTestClient
                .put()
                .uri(REVIEW_URI + "/{id}", "falseId")
                .bodyValue(existingReview)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}