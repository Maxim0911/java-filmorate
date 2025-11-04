
package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = getFilmById(film.getId());
        validateFilm(film);

        film.setLikes(existingFilm.getLikes());

        return filmStorage.update(film);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId); // Проверяем что пользователь существует

        film.getLikes().add(userId);
        filmStorage.update(film); // Сохраняем изменения
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId); // Проверяем что пользователь существует

        film.getLikes().remove(userId);
        filmStorage.update(film); // Сохраняем изменения
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}