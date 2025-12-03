package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма должно быть заполнено.")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Описание фильма не может превышать 200 символов.")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private Mpa mpa;
    private Set<Genre> genres = new HashSet<>();
    private Set<Long> likes = new HashSet<>();

    public Film() {
        this.likes = new HashSet<>();
        this.genres = new HashSet<>();
    }

    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
        this.genres = new HashSet<>();
    }

    public Set<Long> getGenreIds() {
        return genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
    }

    public void setGenreIds(Set<Long> genreIds) {
        if (genreIds != null) {
            this.genres = genreIds.stream()
                    .map(Genre::new)
                    .collect(Collectors.toSet());
        }
    }

    public void addGenreId(Long genreId) {
        this.genres.add(new Genre(genreId));
    }

    public void removeGenreId(Long genreId) {
        this.genres.removeIf(genre -> genre.getId().equals(genreId));
    }

    public Integer getRatingId() {
        return mpa != null ? mpa.getId().intValue() : null;
    }

    public void setRatingId(Integer ratingId) {
        if (ratingId == null) {
            this.mpa = null;
        } else {
            this.mpa = new Mpa(ratingId.longValue(), null, null);
        }
    }
}