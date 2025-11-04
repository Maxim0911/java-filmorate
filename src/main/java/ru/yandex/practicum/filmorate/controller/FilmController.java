package ru.yandex.practicum.filmorate.controller;

import exceptions.NotFoundException;
import exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException e) {
        log.error("Validation error: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e) {
        log.error("Not found error: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return currentMaxId + 1;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        try {
            LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(minReleaseDate)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }

            log.info("Добавление нового фильма: {}", film.getName());

            film.setId(getNextId());
            films.put(film.getId(), film);

            log.info("Новый фильм успешно добавлен. Присвоено ID={}", film.getId());
            return film;
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        try {

            if (newFilm.getId() == null || newFilm.getId() == 0) {
                throw new NotFoundException("Необходимо указать ID фильма.");
            }

            log.info("Обновление фильма с ID {}", newFilm.getId());

            if (!films.containsKey(newFilm.getId())) {
                throw new NotFoundException(format("Фильм с id={0} не найден", newFilm.getId()));
            }

            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
            } else if (newFilm.getName() != null) {
                throw new ValidationException("Название фильма не может быть пустым.");
            }

            if (newFilm.getDescription() != null) {
                if (newFilm.getDescription().length() > 200) {
                    throw new ValidationException("Описание фильма не может превышать 200 символов.");
                }
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {
                LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
                if (newFilm.getReleaseDate().isBefore(minReleaseDate)) {
                    throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
                }
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null) {
                if (newFilm.getDuration() <= 0) {
                    throw new ValidationException("Продолжительность фильма не может быть отрицательным числом, либо равной нулю.");
                }
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Обновлен фильм с ID={}", newFilm.getId());
            return oldFilm;

        } catch (NotFoundException | ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}