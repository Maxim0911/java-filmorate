package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1L;

    @Override
    public Film create(Film film) {
        film.setId(currentId);
        films.put(currentId, film);
        currentId++;
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new exceptions.NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        Film existingFilm = films.get(film.getId());
        Film updatedFilm = new Film();
        updatedFilm.setId(film.getId());
        updatedFilm.setName(film.getName());
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        updatedFilm.setLikes(existingFilm.getLikes());

        films.put(film.getId(), updatedFilm);
        return updatedFilm;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }
}