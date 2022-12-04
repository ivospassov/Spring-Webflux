package com.example.springwebfluxmongodb.client;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import com.example.springwebfluxmongodb.exceptions.MovieInfoClientException;
import com.example.springwebfluxmongodb.exceptions.MovieInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class MoviesInfoRestClient {
    private final WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoURL;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        String url = moviesInfoURL.concat("/{id}");

        return webClient
                .get()
                .uri(url, movieId)
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
                .bodyToMono(MovieInfo.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                .log();
    }
}
