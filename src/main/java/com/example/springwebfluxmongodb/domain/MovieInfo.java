package com.example.springwebfluxmongodb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MovieInfo {

    @Id
    private String movieInfoId;

    @NotBlank(message = "Movie info name must be present")
    private String name;

    @NotNull
    @Positive(message = "Year must be greater than 0")
    private Integer year;


    private List<@NotBlank(message = "Movie cast must be present") String> cast;
    private LocalDate releaseDate;
}
