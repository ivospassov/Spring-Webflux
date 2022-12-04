package com.example.springwebfluxmongodb.controller;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import com.example.springwebfluxmongodb.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MovieInfoController {

    private final MovieInfoService movieInfoService;

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();

    public MovieInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/movieInfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return this.movieInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedMovieInfo -> movieInfoSink.tryEmitNext(savedMovieInfo));
    }

    @GetMapping(value = "/movieInfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getAllMovieInfoStream() {
        return this.movieInfoSink.asFlux().log();
    }

    @GetMapping("/movieInfos")
    public Flux<MovieInfo> getAllMovies(@RequestParam(value = "year", required = false) Integer year,
                                        @RequestParam(value = "name", required = false) String movieName) {
        log.info("Year is {} ", year);

        if (year != null) {
            return this.movieInfoService.getMoviesBeforeYear(year).log();
        } else if (movieName != null) {
            return this.movieInfoService.getMovieByName(movieName).flatMapMany(Flux::just).log();
        }
        return this.movieInfoService.findAll().log();
    }

    @GetMapping("/movieInfos/{id}")
    public Mono<MovieInfo> getMovieInfoById(@PathVariable("id") String movieInfoId) {
        return this.movieInfoService.findById(movieInfoId).log();
    }

    @PutMapping("/movieInfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable("id") String movieInfoId) {
        return this.movieInfoService.updateMovieInfo(updatedMovieInfo, movieInfoId)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieInfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieById(@PathVariable("id") String movieInfoId) {
        return this.movieInfoService.deleteById(movieInfoId).log();
    }
}
