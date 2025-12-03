package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(FilmDbStorage.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    void setUp() {
        // Очищаем и инициализируем тестовые данные
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM ratings");
        jdbcTemplate.update("DELETE FROM genres");

        // Инициализируем справочные данные
        jdbcTemplate.update("INSERT INTO ratings (id, name, description) VALUES (1, 'G', 'General audiences')");
        jdbcTemplate.update("INSERT INTO ratings (id, name, description) VALUES (2, 'PG', 'Parental guidance')");

        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (2, 'Драма')");
    }

    @Test
    void create_ShouldCreateFilmSuccessfully() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        // Act
        Film createdFilm = filmDbStorage.create(film);

        // Assert
        assertNotNull(createdFilm);
        assertThat(createdFilm.getId()).isPositive();
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(120, createdFilm.getDuration());
        assertNotNull(createdFilm.getMpa());
        assertEquals(1L, createdFilm.getMpa().getId());
    }

    @Test
    void create_ShouldCreateFilmWithGenres() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        film.setGenres(Set.of(new Genre(1L, "Комедия")));

        // Act
        Film createdFilm = filmDbStorage.create(film);

        // Assert
        assertNotNull(createdFilm);
        assertThat(createdFilm.getGenres()).hasSize(1);
        assertEquals(1L, createdFilm.getGenres().iterator().next().getId());
        assertEquals("Комедия", createdFilm.getGenres().iterator().next().getName());
    }

    @Test
    void findById_ShouldReturnFilmWhenExists() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        // Act
        Optional<Film> foundFilm = filmDbStorage.findById(filmId);

        // Assert
        assertTrue(foundFilm.isPresent());
        assertEquals("Test Film", foundFilm.get().getName());
        assertEquals(filmId, foundFilm.get().getId());
        assertNotNull(foundFilm.get().getMpa());
        assertEquals(1L, foundFilm.get().getMpa().getId());
    }

    @Test
    void findById_ShouldReturnEmptyOptionalWhenFilmNotFound() {
        // Act
        Optional<Film> foundFilm = filmDbStorage.findById(999L);

        // Assert
        assertFalse(foundFilm.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1L, "G", null));
        filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(150);
        film2.setMpa(new Mpa(2L, "PG", null));
        filmDbStorage.create(film2);

        // Act
        List<Film> films = filmDbStorage.findAll();

        // Assert
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film One", "Film Two");
    }

    @Test
    void update_ShouldUpdateFilmSuccessfully() {
        // Arrange
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        Film filmToUpdate = new Film();
        filmToUpdate.setId(filmId);
        filmToUpdate.setName("Updated Film");
        filmToUpdate.setDescription("Updated Description");
        filmToUpdate.setReleaseDate(LocalDate.of(2001, 1, 1));
        filmToUpdate.setDuration(150);
        filmToUpdate.setMpa(new Mpa(2L, "PG", null));

        // Act
        Film updatedFilm = filmDbStorage.update(filmToUpdate);

        // Assert
        assertNotNull(updatedFilm);
        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());
        assertEquals(2L, updatedFilm.getMpa().getId());

        // Verify in database
        Optional<Film> foundFilm = filmDbStorage.findById(filmId);
        assertTrue(foundFilm.isPresent());
        assertEquals("Updated Film", foundFilm.get().getName());
    }

    @Test
    void update_ShouldUpdateFilmGenres() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        Film filmToUpdate = new Film();
        filmToUpdate.setId(filmId);
        filmToUpdate.setName("Updated Film");
        filmToUpdate.setDescription("Updated Description");
        filmToUpdate.setReleaseDate(LocalDate.of(2001, 1, 1));
        filmToUpdate.setDuration(150);
        filmToUpdate.setMpa(new Mpa(1L, "G", null));
        filmToUpdate.setGenres(Set.of(
                new Genre(1L, "Комедия"),
                new Genre(2L, "Драма")
        ));

        // Act
        Film updatedFilm = filmDbStorage.update(filmToUpdate);

        // Assert
        assertNotNull(updatedFilm);
        assertThat(updatedFilm.getGenres()).hasSize(2);
        assertThat(updatedFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(updatedFilm.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма");
    }

    @Test
    void delete_ShouldDeleteFilmSuccessfully() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        // Act
        filmDbStorage.delete(filmId);

        // Assert
        Optional<Film> deletedFilm = filmDbStorage.findById(filmId);
        assertFalse(deletedFilm.isPresent());
    }

    @Test
    void addLike_ShouldAddLikeSuccessfully() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        // Create a user for like
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'user@test.ru', 'user1', 'User One', '1990-01-01')");

        // Act
        filmDbStorage.addLike(filmId, 1L);

        // Assert
        int likesCount = filmDbStorage.getLikesCount(filmId);
        assertEquals(1, likesCount);

        Optional<Film> filmWithLike = filmDbStorage.findById(filmId);
        assertTrue(filmWithLike.isPresent());
        assertTrue(filmWithLike.get().getLikes().contains(1L));
    }

    @Test
    void removeLike_ShouldRemoveLikeSuccessfully() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'user@test.ru', 'user1', 'User One', '1990-01-01')");
        filmDbStorage.addLike(filmId, 1L);

        // Act
        filmDbStorage.removeLike(filmId, 1L);

        // Assert
        int likesCount = filmDbStorage.getLikesCount(filmId);
        assertEquals(0, likesCount);

        Optional<Film> filmWithoutLike = filmDbStorage.findById(filmId);
        assertTrue(filmWithoutLike.isPresent());
        assertFalse(filmWithoutLike.get().getLikes().contains(1L));
    }

    @Test
    void getLikesCount_ShouldReturnCorrectCount() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'user1@test.ru', 'user1', 'User One', '1990-01-01')");
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (2, 'user2@test.ru', 'user2', 'User Two', '1990-02-02')");

        filmDbStorage.addLike(filmId, 1L);
        filmDbStorage.addLike(filmId, 2L);

        // Act
        int likesCount = filmDbStorage.getLikesCount(filmId);

        // Assert
        assertEquals(2, likesCount);
    }

    @Test
    void getLikesCount_ShouldReturnZeroForFilmWithoutLikes() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        // Act
        int likesCount = filmDbStorage.getLikesCount(filmId);

        // Assert
        assertEquals(0, likesCount);
    }
}