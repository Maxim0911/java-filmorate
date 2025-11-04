
package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID={}", id);
        return filmService.getFilmById(id);
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
}