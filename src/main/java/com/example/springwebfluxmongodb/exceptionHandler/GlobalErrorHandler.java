package com.example.springwebfluxmongodb.exceptionHandler;

import com.example.springwebfluxmongodb.exceptions.MovieInfoClientException;
import com.example.springwebfluxmongodb.exceptions.ReviewDataException;
import com.example.springwebfluxmongodb.exceptions.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyError(WebExchangeBindException exception) {
        log.error("Exception Caught in HandleRequestBodyError: {} ", exception.getMessage(), exception);
        String error = exception.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(", "));

        log.error("Error is: {} ", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MovieInfoClientException.class)
    public ResponseEntity<String> handleClientException(MovieInfoClientException exception) {
        log.error("Exception caught in HandleClientException : {} ", exception.getMessage(), exception);
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        log.error("Exception caught in HandleRuntimeException : {} ", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Exception message is : {}", ex.getMessage(), ex);
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer errorMessage = dataBufferFactory.wrap(ex.getMessage().getBytes());

        if (ex instanceof ReviewDataException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().writeWith(Mono.just(errorMessage));
        }

        if (ex instanceof ReviewNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().writeWith(Mono.just(errorMessage));
        }

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(errorMessage));
    }
}
