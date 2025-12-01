package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {
        log.info("Создание фильма: {}", film.getName());
        validateFilm(film);

        if (film.getRatingId() != null) {
            validateMpaExists(film.getRatingId());
        }

        validateGenres(film);

        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
        return createdFilm;
    }

    public Film update(Film film) {
        log.info("Обновление фильма с ID: {}", film.getId());

        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        validateFilm(film);

        if (film.getRatingId() != null) {
            validateMpaExists(film.getRatingId());
        }

        validateGenres(film);

        film.setLikes(existingFilm.getLikes());

        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID: {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    public List<Film> findAll() {
        List<Film> films = filmStorage.findAll();
        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));

        log.info("Найден фильм: ID={}, название={}", film.getId(), film.getName());
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм={}, пользователь={}", filmId, userId);

        getFilmById(filmId);
        userService.getUserById(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Лайк добавлен: фильм={}, пользователь={}", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм={}, пользователь={}", filmId, userId);

        getFilmById(filmId);
        userService.getUserById(userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Лайк удален: фильм={}, пользователь={}", filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        List<Film> allFilms = filmStorage.findAll();

        List<Film> sortedFilms = allFilms.stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());

        int limit = (count == null || count <= 0) ? 10 : count;
        List<Film> popularFilms = sortedFilms.stream()
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Возвращаем {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpaExists(Integer ratingId) {
        if (!mpaStorage.existsById(ratingId)) {
            throw new NotFoundException("Рейтинг MPA с id=" + ratingId + " не найден");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
    }

    public List<Mpa> getAllMpa() {
        List<Mpa> mpaList = mpaStorage.findAll();
        log.info("Найдено {} рейтингов MPA", mpaList.size());
        return mpaList;
    }

    public Mpa getMpaById(Long id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + id + " не найден"));
    }

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreStorage.findAll();
        log.info("Найдено {} жанров", genres.size());
        return genres;
    }

    public Genre getGenreById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }

    public void addGenreToFilm(Long filmId, Long genreId) {
        log.info("Добавление жанра ID={} к фильму ID={}", genreId, filmId);

        Film film = getFilmById(filmId);
        Genre genre = getGenreById(genreId);

        film.getGenres().add(genre);

        filmStorage.update(film);

        log.info("Жанр ID={} успешно добавлен к фильму ID={}", genreId, filmId);
    }

    public void removeGenreFromFilm(Long filmId, Long genreId) {
        log.info("Удаление жанра ID={} из фильма ID={}", genreId, filmId);

        Film film = getFilmById(filmId);

        boolean removed = film.getGenres().removeIf(genre -> genre.getId().equals(genreId));

        if (removed) {
            filmStorage.update(film);
            log.info("Жанр ID={} успешно удален из фильма ID={}", genreId, filmId);
        } else {
            log.warn("Жанр ID={} не найден у фильма ID={}", genreId, filmId);
        }
    }

    public Set<Genre> getFilmGenres(Long filmId) {
        log.info("Получение жанров фильма ID={}", filmId);
        Film film = getFilmById(filmId);
        return film.getGenres();
    }
}