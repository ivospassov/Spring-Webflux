package com.example.springwebfluxmongodb.router;

import com.example.springwebfluxmongodb.domain.Review;
import com.example.springwebfluxmongodb.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReviewRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEW_URI = "/v1/reviews";

    @BeforeEach
    void setUp() {

        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome movie", 9.0),
                new Review(null, 1L, "Awesome movie1", 9.0),
                new Review("abc", 2L, "Excellent movie", 8.0));

        this.reviewReactiveRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {

        Review newReview = new Review(null, 1L, "Awesome Movie", 9.0);

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
    void getAllReviews() {

        webTestClient
                .get()
                .uri(REVIEW_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {

        Review updatedReview = new Review("abc", 1L, "Next Awesome Movie", 9.5);

        webTestClient
                .put()
                .uri(REVIEW_URI + "/{id}", "abc")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.comment").isEqualTo("Next Awesome Movie")
                .jsonPath("$.rating").isEqualTo(9.5);
    }

    @Test
    void deleteReviewById() {

        webTestClient
                .delete()
                .uri(REVIEW_URI + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void findReviewsByMovieInfoId() {

        URI uriWithQueryParam = UriComponentsBuilder
                .fromUriString(REVIEW_URI)
                .queryParam("movieInfoId", 1)
                .buildAndExpand()
                .toUri();

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
    void getReviewById() {

        Review review = webTestClient
                .get()
                .uri(REVIEW_URI + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .returnResult()
                .getResponseBody();

        assert review != null;
        assert review.getReviewId() != null;
    }
}