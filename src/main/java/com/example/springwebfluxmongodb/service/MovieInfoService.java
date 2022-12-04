package com.example.springwebfluxmongodb.service;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoService {

    Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo);

    Flux<MovieInfo> findAll();

    Mono<MovieInfo> findById(String movieInfoId);

    Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String movieInfoId);

    Mono<Void> deleteById(String movieInfoId);

    Flux<MovieInfo> getMoviesBeforeYear(Integer beforeYear);

    Mono<MovieInfo> getMovieByName(String movieName);
}
