package com.example.springwebfluxmongodb.repository;

import com.example.springwebfluxmongodb.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {
    Flux<Review> findByMovieInfoId(Long movieInfoId);
}
