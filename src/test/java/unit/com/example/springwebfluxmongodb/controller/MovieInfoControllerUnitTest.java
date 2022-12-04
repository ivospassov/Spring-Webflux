package com.example.springwebfluxmongodb.controller;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import com.example.springwebfluxmongodb.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static com.example.springwebfluxmongodb.controller.MovieInfoControllerTest.MOVIE_INFO_URL;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
class MovieInfoControllerUnitTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MovieInfoService movieInfoServiceMock;

    static String MOVIE_INFO_URI = "/v1/movieInfos";

    @Test
    void getAllMovieInfos() {
        List<MovieInfo> moviesInfoList = List.of(
                new MovieInfo(null, "BatmanBegins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(movieInfoServiceMock.findAll()).thenReturn(Flux.fromIterable(moviesInfoList));

        webTestClient
                .get()
                .uri(MOVIE_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfosById() {
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012,
                List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.findById("abc")).thenReturn(Mono.just(movieInfo));

        webTestClient
                .get()
                .uri(MOVIE_INFO_URL + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name" ).isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises", 2012,
                List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        ));

        webTestClient
                .post()
                .uri(MOVIE_INFO_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises")
                .jsonPath("$.movieInfoId").isEqualTo("abc");
    }

    @Test
    void updateMovieInfo() {
        String mockId = "mockId";

        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises", 2012,
                List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(
                        new MovieInfo(mockId, "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")))
                );

        webTestClient
                .put()
                .uri(MOVIE_INFO_URI + "/{id}", mockId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();

                    assert responseBody != null;
                    assert responseBody.getMovieInfoId() != null;
                });
    }

    @Test
    void deleteMovieById() {
        String mockId = "mockId";

        when(movieInfoServiceMock.deleteById(mockId)).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIE_INFO_URL + "/{id}", mockId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void addMovieInfoValidation() {
        MovieInfo movieInfo = new MovieInfo(null, "", -2005,
                List.of(""), LocalDate.parse("2012-07-20"));

        webTestClient
                .post()
                .uri(MOVIE_INFO_URI)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();

                    assert responseBody != null;
                    System.out.println("Response body: " + responseBody);

                    String expectedErrorMessage = "Movie cast must be present, Movie info name must be present, Year must be greater than 0";
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void updateMovieNotFound() {
        String movieInfoId = "def";
        MovieInfo movieInfo = new MovieInfo(movieInfoId, "Dark Knight Rises", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .put()
                .uri(MOVIE_INFO_URI + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}