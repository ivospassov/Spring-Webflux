package com.example.springwebfluxmongodb.repository;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        Flux<MovieInfo> moviesInfoFlux = this.movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        Mono<MovieInfo> documentById = this.movieInfoRepository.findById("abc").log();

        StepVerifier.create(documentById)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "BatmanvSuperman", 2017,
                List.of("Ben Afleck", "Henry Cavill"), LocalDate.parse("2017-10-19"));

        Mono<MovieInfo> movieEntity = this.movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(movieEntity)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("BatmanvSuperman", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        MovieInfo movieInfo = this.movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        Mono<MovieInfo> updatedMovie = this.movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(updatedMovie)
                .assertNext(movieInfo1 -> {
                    assertEquals(2021, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        this.movieInfoRepository.deleteById("abc").block();

        Flux<MovieInfo> movieInfoList = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoList)
                .expectNextCount(2)
                .verifyComplete();
    }
}