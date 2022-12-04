package com.example.springwebfluxmongodb.controller;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import com.example.springwebfluxmongodb.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieInfoRepository movieInfoRepository;

    static String MOVIE_INFO_URL = "/v1/movieInfos";

    @BeforeEach
    void setUp() {
        List<MovieInfo> moviesInfoList = List.of(
                new MovieInfo(null, "BatmanBegins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        this.movieInfoRepository.saveAll(moviesInfoList).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();

                    assert responseBody != null;
                    assertEquals("Batman Begins1", responseBody.getName());
                });
    }

    @Test
    void getAllMovies_Stream() {

        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();

                    assert responseBody != null;
                    assertEquals("Batman Begins1", responseBody.getName());
                });

        Flux<MovieInfo> movieStreamFlux = webTestClient
                .get()
                .uri(MOVIE_INFO_URL + "/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier
                .create(movieStreamFlux)
                .assertNext(movieInfoObject -> {
                    assert movieInfoObject.getMovieInfoId() != null;
                })
                .thenCancel()
                .verify();
    }

    @Test
    void getAllMovies() {

        webTestClient
                .get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMoviesBeforeYear() {
        URI uri = UriComponentsBuilder
                .fromUriString(MOVIE_INFO_URL)
                .queryParam("year", 2009)
                .buildAndExpand()
                .toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    void getMovieInfoById() {

        webTestClient
                .get()
                .uri(MOVIE_INFO_URL + "/{id}", "111")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name" ).isEqualTo("Dark Knight Rises")
                /*.expectBody(MovieInfo.class)
                .consumeWith(movieInfo -> {
                    MovieInfo movieinfoResponseBody = movieInfo.getResponseBody();

                    assert movieinfoResponseBody != null;
                    assertEquals("Dark Knight Rises", movieinfoResponseBody.getName());
                })*/
                ;
    }

    @Test
    void getMovieInfoByName() {
        URI uri = UriComponentsBuilder
                .fromUriString(MOVIE_INFO_URL)
                .queryParam("name", "BatmanBegins")
                .buildAndExpand()
                .toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .consumeWith(listEntityExchangeResult -> {
                    MovieInfo responseBody = Objects.requireNonNull(listEntityExchangeResult.getResponseBody()).get(0);

                    assert responseBody != null;
                    assertEquals("BatmanBegins", responseBody.getName());
                    assertEquals(2005, responseBody.getYear());
                    assertEquals(List.of("Christian Bale", "Michael Cane"), responseBody.getCast());
                    assertEquals("2005-06-15", responseBody.getReleaseDate().toString());
                });
    }

    @Test
    void updateMovieInfo() {
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises 1", 2014,
                List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2014-07-20"));

        webTestClient
                .put()
                .uri(MOVIE_INFO_URL + "/{id}", "abc")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.year").isEqualTo(2014)
                .jsonPath("$.releaseDate").isEqualTo("2014-07-20");
    }

    @Test
    void deleteMovieById() {

        webTestClient
                .delete()
                .uri(MOVIE_INFO_URL + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}