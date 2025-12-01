package ru.yandex.practicum.filmorate.controller;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RaitingFilm;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmRequest {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private RaitingFilm mpa;
    private Set<Genre> genres;

    public FilmRequest() {
    }

    public FilmRequest(String name, String description, LocalDate releaseDate, Integer duration, RaitingFilm mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }
}