
import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createFilm_WithValidData_ShouldSuccess() {
        Film result = filmController.create(validFilm);

        assertNotNull(result.getId());
        assertEquals("Valid Film", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    void createFilm_WithEmptyName_ShouldThrowException() {
        validFilm.setName("");

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithNullName_ShouldThrowException() {
        validFilm.setName(null);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithBlankName_ShouldThrowException() {
        validFilm.setName("   ");

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithTooLongDescription_ShouldThrowException() {
        String longDescription = "A".repeat(201);
        validFilm.setDescription(longDescription);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithExact200SymbolDescription_ShouldSuccess() {
        String exactLengthDescription = "A".repeat(200);
        validFilm.setDescription(exactLengthDescription);

        Film result = filmController.create(validFilm);
        assertEquals(exactLengthDescription, result.getDescription());
    }

    @Test
    void createFilm_WithNullDescription_ShouldThrowException() {
        validFilm.setDescription(null);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithReleaseDateBefore1895_ShouldThrowException() {
        validFilm.setReleaseDate(LocalDate.of(1894, 12, 31));

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithReleaseDateExactly1895_12_28_ShouldSuccess() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film result = filmController.create(validFilm);
        assertEquals(LocalDate.of(1895, 12, 28), result.getReleaseDate());
    }

    @Test
    void createFilm_WithReleaseDateOneDayBeforeMinDate_ShouldThrowException() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithNullReleaseDate_ShouldThrowException() {
        validFilm.setReleaseDate(null);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldThrowException() {
        validFilm.setDuration(-10);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithZeroDuration_ShouldThrowException() {
        validFilm.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(validFilm));
    }

    @Test
    void createFilm_WithPositiveDuration_ShouldSuccess() {
        validFilm.setDuration(1); // Минимальная положительная продолжительность

        Film result = filmController.create(validFilm);
        assertEquals(1, result.getDuration());
    }

    @Test
    void updateFilm_WithValidData_ShouldSuccess() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Film");
        updateFilm.setDescription("Updated description");
        updateFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        updateFilm.setDuration(150);

        Film result = filmController.update(updateFilm);

        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    void updateFilm_WithInvalidId_ShouldThrowException() {
        Film updateFilm = new Film();
        updateFilm.setId(0L); // Невалидный ID

        assertThrows(NotFoundException.class, () -> filmController.update(updateFilm));
    }

    @Test
    void updateFilm_WithNegativeId_ShouldThrowException() {
        Film updateFilm = new Film();
        updateFilm.setId(-1L); // Отрицательный ID

        assertThrows(NotFoundException.class, () -> filmController.update(updateFilm));
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldThrowException() {
        Film updateFilm = new Film();
        updateFilm.setId(999L); // Несуществующий ID
        updateFilm.setName("Some Name");

        assertThrows(NotFoundException.class, () -> filmController.update(updateFilm));
    }

    @Test
    void updateFilm_PartialUpdate_ShouldSuccess() {
        Film createdFilm = filmController.create(validFilm);

        Film partialUpdate = new Film();
        partialUpdate.setId(createdFilm.getId());
        partialUpdate.setName("Only Name Updated");
        // Не устанавливаем duration - должно остаться старое значение

        Film result = filmController.update(partialUpdate);

        assertEquals("Only Name Updated", result.getName());
        assertEquals("Valid description", result.getDescription()); // Осталось прежним
        assertEquals(120, result.getDuration()); // Осталось прежним
    }

    @Test
    void updateFilm_WithEmptyName_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName(""); // Пустое название

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm));
    }

    @Test
    void updateFilm_WithTooLongDescription_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDescription("A".repeat(201)); // Слишком длинное описание

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm));
    }

    @Test
    void updateFilm_WithFutureReleaseDate_ShouldSuccess() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setReleaseDate(LocalDate.now().plusDays(1)); // Фильм еще не вышел
        updateFilm.setDuration(150); // ✅ ДОБАВЬТЕ валидную продолжительность

        Film result = filmController.update(updateFilm);
        assertEquals(LocalDate.now().plusDays(1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        filmController.create(validFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another description");
        anotherFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        anotherFilm.setDuration(90);
        filmController.create(anotherFilm);

        Collection<Film> films = filmController.findAll();

        assertEquals(2, films.size());
    }

    @Test
    void findAll_WhenNoFilms_ShouldReturnEmptyCollection() {
        Collection<Film> films = filmController.findAll();

        assertTrue(films.isEmpty());
    }

    // Тесты на граничные условия
    @Test
    void createFilm_WithMaxDuration_ShouldSuccess() {
        validFilm.setDuration(Integer.MAX_VALUE);

        Film result = filmController.create(validFilm);
        assertEquals(Integer.MAX_VALUE, result.getDuration());
    }

    @Test
    void createFilm_WithCurrentDate_ShouldSuccess() {
        validFilm.setReleaseDate(LocalDate.now());

        Film result = filmController.create(validFilm);
        assertEquals(LocalDate.now(), result.getReleaseDate());
    }

    @Test
    void updateFilm_WithNullDuration_ShouldUseOldValue() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Updated Name");
        // Duration не устанавливаем - должен остаться старый

        Film result = filmController.update(updateFilm);
        assertEquals("Updated Name", result.getName());
        assertEquals(120, result.getDuration()); // Старое значение сохранилось
    }
}