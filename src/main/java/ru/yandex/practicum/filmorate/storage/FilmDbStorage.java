package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Repository
@Slf4j
@Primary
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            if (film.getRatingId() != null) {
                stmt.setInt(5, film.getRatingId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        saveGenres(filmId, film.getGenreIds());
        return findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film not found after creation: " + filmId));
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRatingId(),
                film.getId()
        );

        if (updated == 0) {
            throw new RuntimeException("Film not found with id: " + film.getId());
        }

        saveGenres(film.getId(), film.getGenreIds());
        return findById(film.getId())
                .orElseThrow(() -> new RuntimeException("Film not found after update: " + film.getId()));
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.*, r.name as mpa_name, r.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id";
        return jdbcTemplate.query(sql, this::mapFilm);
    }

    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.id as film_id, f.name as film_name, f.description, " +
                "f.release_date, f.duration, f.rating_id, " +
                "r.id as mpa_id, r.name as mpa_name, r.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        log.debug("Executing SQL: {}", sql);
        List<Film> films = jdbcTemplate.query(sql, this::mapFilm, id);
        return films.stream().findFirst();
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", id);
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            if (!e.getMessage().contains("unique constraint") &&
                    !e.getMessage().contains("duplicate key")) {
                throw new RuntimeException("Ошибка при добавлении лайка: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null ? count : 0;
    }

    private Film mapFilm(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Long ratingId = rs.getLong("rating_id");
        if (ratingId != 0) {  // или !rs.wasNull()
            Mpa mpa = loadMpaFromDb(ratingId);
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        Set<Genre> genres = loadGenres(film.getId());
        film.setGenres(genres);
        Set<Long> likes = loadLikes(film.getId());
        film.setLikes(likes);

        return film;
    }

    private Set<Genre> loadGenres(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genre fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";

        try {
            List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                return genre;
            }, filmId);

            return new LinkedHashSet<>(genres);
        } catch (Exception e) {
            log.error("Error loading genres for film {}: {}", filmId, e.getMessage());
            return new HashSet<>();
        }
    }

    private Mpa loadMpaFromDb(Long ratingId) {
        String sql = "SELECT id, name, description FROM ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                            new Mpa(rs.getLong("id"), rs.getString("name"), rs.getString("description")),
                    ratingId);
        } catch (Exception e) {
            log.error("Failed to load MPA with id {}: {}", ratingId, e.getMessage());
            return new Mpa(ratingId, "Unknown", null);
        }
    }

    private Set<Long> loadLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        try {
            List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, filmId);
            return new HashSet<>(likes);
        } catch (Exception e) {
            log.error("Error loading likes for film {}: {}", filmId, e.getMessage());
            return new HashSet<>();
        }
    }

    private void saveGenres(Long filmId, Set<Long> genreIds) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId);

        if (genreIds != null && !genreIds.isEmpty()) {
            String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Long genreId : genreIds) {
                jdbcTemplate.update(insertSql, filmId, genreId);
            }
        }
    }
}