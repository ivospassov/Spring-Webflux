package com.example.springwebfluxmongodb.client;

import com.example.springwebfluxmongodb.domain.Review;
import com.example.springwebfluxmongodb.exceptions.MovieInfoClientException;
import com.example.springwebfluxmongodb.exceptions.MovieInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReviewsRestClient {

    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsURL;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {

        String url = UriComponentsBuilder.fromHttpUrl(reviewsURL)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toString();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {} ", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(
                                new MovieInfoClientException("There is no MovieInfo Available for the passed in Id"
                                        + movieId, clientResponse.statusCode().value()));
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(
                                    new MovieInfoClientException(responseMessage, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is : {} ", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MovieInfoServerException(responseMessage)));
                })
                .bodyToFlux(Review.class)
                .log();
    }
}
