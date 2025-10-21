
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
    void createFilm_WithReleaseDateBefore1895_ShouldThrowException() {
        validFilm.setReleaseDate(LocalDate.of(1894, 12, 31));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(validFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(validFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
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
        updateFilm.setId(0L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Необходимо указать ID фильма.", exception.getMessage());
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldThrowException() {
        Film updateFilm = new Film();
        updateFilm.setId(999L);
        updateFilm.setName("Some Name");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.update(updateFilm));
        assertTrue(exception.getMessage().contains("Фильм с id=999 не найден"));
    }

    @Test
    void updateFilm_PartialUpdate_ShouldSuccess() {
        Film createdFilm = filmController.create(validFilm);

        Film partialUpdate = new Film();
        partialUpdate.setId(createdFilm.getId());
        partialUpdate.setName("Only Name Updated");

        Film result = filmController.update(partialUpdate);

        assertEquals("Only Name Updated", result.getName());
        assertEquals("Valid description", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    void updateFilm_WithEmptyName_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Название фильма не может быть пустым.", exception.getMessage());
    }

    @Test
    void updateFilm_WithTooLongDescription_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDescription("A".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Описание фильма не может превышать 200 символов.", exception.getMessage());
    }

    @Test
    void updateFilm_WithNegativeDuration_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Продолжительность фильма не может быть отрицательным числом, либо равной нулю.", exception.getMessage());
    }

    @Test
    void updateFilm_WithZeroDuration_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Продолжительность фильма не может быть отрицательным числом, либо равной нулю.", exception.getMessage());
    }

    @Test
    void updateFilm_WithOldReleaseDate_ShouldThrowException() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updateFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года.", exception.getMessage());
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
        // duration остается null

        Film result = filmController.update(updateFilm);
        assertEquals("Updated Name", result.getName());
        assertEquals(120, result.getDuration()); // старое значение сохранилось
    }

    @Test
    void updateFilm_WithNullName_ShouldUseOldValue() {
        Film createdFilm = filmController.create(validFilm);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setDescription("New Description");
        // name остается null

        Film result = filmController.update(updateFilm);
        assertEquals("Valid Film", result.getName()); // старое значение сохранилось
        assertEquals("New Description", result.getDescription());
    }
}