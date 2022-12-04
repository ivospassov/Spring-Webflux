package com.example.springwebfluxmongodb.repository;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {

    Flux<MovieInfo> findByYearBefore(Integer year);

    Mono<MovieInfo> findByName(String movieName);
}
