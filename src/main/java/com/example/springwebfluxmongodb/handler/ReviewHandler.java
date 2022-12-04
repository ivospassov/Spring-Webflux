package com.example.springwebfluxmongodb.handler;

import com.example.springwebfluxmongodb.domain.Review;
import com.example.springwebfluxmongodb.exceptions.ReviewDataException;
import com.example.springwebfluxmongodb.exceptions.ReviewNotFoundException;
import com.example.springwebfluxmongodb.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private final ReviewReactiveRepository reviewReactiveRepository;

    private Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(this.reviewReactiveRepository::save)
                .doOnNext(this.reviewsSink::tryEmitNext)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> constraintViolations = this.validator.validate(review);
        log.info("constraint violations : {}", constraintViolations);

        if (!constraintViolations.isEmpty()) {
            String errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));

            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest serverRequest) {
        Optional<String> movieInfoId = serverRequest.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            Flux<Review> reviewsByMovieInfoId = this.reviewReactiveRepository.findByMovieInfoId(Long.parseLong(movieInfoId.get()));
            return getServerResponseMono(reviewsByMovieInfoId);
        }

        Flux<Review> allReviewsFlux = this.reviewReactiveRepository.findAll();
        return getServerResponseMono(allReviewsFlux);
    }

    /**
     * Return the server response for get query (includes also with query parameter: movieInfoId)
     * @param reviewsByMovieInfoId - extracted reviews by movieInfoId
     * @return Mono<ServerResponse>
     */
    private Mono<ServerResponse> getServerResponseMono(Flux<Review> reviewsByMovieInfoId) {
        return ServerResponse.ok().body(reviewsByMovieInfoId, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {

        String reviewId = serverRequest.pathVariable("id");
        Mono<Review> existingReview = this.reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id : " + reviewId)));

        return existingReview
                .flatMap(review -> serverRequest.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            review.setMovieInfoId(reqReview.getMovieInfoId());
                            review.setReviewId(reqReview.getReviewId());
                            return review;
                        }))
                .flatMap(this.reviewReactiveRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.OK).bodyValue(savedReview));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {

        String reviewId = serverRequest.pathVariable("id");
        Mono<Review> existingReview = this.reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> this.reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }

    public Mono<ServerResponse> getReviewById(ServerRequest serverRequest) {
        String reviewId = serverRequest.pathVariable("id");

        Mono<Review> existingReview = this.reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id : " + reviewId)));;

        return ServerResponse.status(HttpStatus.OK).body(existingReview, Review.class);
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {
        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
