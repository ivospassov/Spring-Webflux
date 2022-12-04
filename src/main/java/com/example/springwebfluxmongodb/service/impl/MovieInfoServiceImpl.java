package com.example.springwebfluxmongodb.service.impl;

import com.example.springwebfluxmongodb.domain.MovieInfo;
import com.example.springwebfluxmongodb.repository.MovieInfoRepository;
import com.example.springwebfluxmongodb.service.MovieInfoService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoServiceImpl implements MovieInfoService {
    private final MovieInfoRepository movieInfoRepository;

    public MovieInfoServiceImpl(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return this.movieInfoRepository.save(movieInfo);
    }

    @Override
    public Flux<MovieInfo> findAll() {
        return this.movieInfoRepository.findAll();
    }

    @Override
    public Mono<MovieInfo> findById(String movieInfoId) {
        return this.movieInfoRepository.findById(movieInfoId);
    }

    @Override
    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String movieInfoId) {
        return this.movieInfoRepository
                .findById(movieInfoId)
                .flatMap(movieInfo -> {
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    @Override
    public Mono<Void> deleteById(String movieInfoId) {
        return this.movieInfoRepository.deleteById(movieInfoId);
    }

    @Override
    public Flux<MovieInfo> getMoviesBeforeYear(Integer beforeYear) {
        return this.movieInfoRepository.findByYearBefore(beforeYear);
    }

    @Override
    public Mono<MovieInfo> getMovieByName(String movieName) {
        return this.movieInfoRepository.findByName(movieName);
    }
}
