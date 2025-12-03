package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID={}", id);
        return filmService.getFilmById(id); // Используем существующий метод
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film.getName());
        Film createdFilm = filmService.create(film);
        log.info("Фильм успешно добавлен с ID={}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID={}", film.getId());
        Film updatedFilm = filmService.update(film);
        log.info("Фильм с ID={} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка фильму ID={} от пользователя ID={}", id, userId);
        filmService.addLike(id, userId);
        log.info("Лайк успешно добавлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка фильму ID={} от пользователя ID={}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Лайк успешно удален");
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) Integer count) {
        log.info("Получен запрос на получение популярных фильмов, count={}", count);
        return filmService.getPopularFilms(count);
    }

    @PutMapping("/{id}/genres/{genreId}")
    public void addGenreToFilm(@PathVariable Long id, @PathVariable Long genreId) {
        log.info("Получен запрос на добавление жанра ID={} фильму ID={}", genreId, id);
        filmService.addGenreToFilm(id, genreId);
        log.info("Жанр успешно добавлен к фильму");
    }

    @DeleteMapping("/{id}/genres/{genreId}")
    public void removeGenreFromFilm(@PathVariable Long id, @PathVariable Long genreId) {
        log.info("Получен запрос на удаление жанра ID={} у фильма ID={}", genreId, id);
        filmService.removeGenreFromFilm(id, genreId);
        log.info("Жанр успешно удален из фильма");
    }

    @GetMapping("/{id}/genres")
    public Set<Genre> getFilmGenres(@PathVariable Long id) {
        log.info("Получен запрос на получение жанров фильма ID={}", id);
        return filmService.getFilmGenres(id);
    }

    @GetMapping("/likes-status")
    public Map<String, Object> likesStatus() {
        List<Film> films = filmService.findAll();

        long totalLikes = films.stream().mapToInt(f -> f.getLikes().size()).sum();
        long filmsWithLikes = films.stream().filter(f -> !f.getLikes().isEmpty()).count();

        List<Map<String, Object>> topFilms = films.stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(5)
                .map(f -> Map.of(
                        "id", f.getId(),
                        "name", f.getName(),
                        "likes", f.getLikes().size(),
                        "likeUsers", f.getLikes()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "totalFilms", films.size(),
                "filmsWithLikes", filmsWithLikes,
                "filmsWithZeroLikes", films.size() - filmsWithLikes,
                "totalLikes", totalLikes,
                "topFilmsByLikes", topFilms
        );
    }

    private Integer getLikesCount(List<Map<String, Object>> likes, Long filmId) {
        return likes.stream()
                .filter(l -> filmId.equals(l.get("FILM_ID")))
                .findFirst()
                .map(l -> ((Number) l.get("LIKES")).intValue())
                .orElse(0);
    }

    private String getCurrentOrder(int pos1, int pos2, int pos3) {
        if (pos1 == -1 || pos2 == -1 || pos3 == -1) return "SOME FILMS MISSING";
        if (pos1 < pos3 && pos3 < pos2) return "film1 > film3 > film2 ✅";
        if (pos1 < pos2 && pos2 < pos3) return "film1 > film2 > film3";
        if (pos2 < pos1 && pos1 < pos3) return "film2 > film1 > film3";
        if (pos2 < pos3 && pos3 < pos1) return "film2 > film3 > film1";
        if (pos3 < pos1 && pos1 < pos2) return "film3 > film1 > film2";
        if (pos3 < pos2 && pos2 < pos1) return "film3 > film2 > film1";
        return "UNKNOWN ORDER";
    }
}