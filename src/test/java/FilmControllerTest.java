

import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmControllerTest {

    @Mock
    private FilmService filmService;

    @InjectMocks
    private FilmController filmController;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setId(1L);
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        // Given
        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Another Film");
        List<Film> films = Arrays.asList(validFilm, film2);
        when(filmService.findAll()).thenReturn(films);

        // When
        List<Film> result = filmController.findAll();

        // Then
        assertEquals(2, result.size());
        verify(filmService, times(1)).findAll();
    }

    @Test
    void getFilm_WithValidId_ShouldReturnFilm() {
        // Given
        when(filmService.getFilmById(1L)).thenReturn(validFilm);

        // When
        Film result = filmController.getFilm(1L);

        // Then
        assertNotNull(result);
        assertEquals("Valid Film", result.getName());
        verify(filmService, times(1)).getFilmById(1L);
    }

    @Test
    void getFilm_WithInvalidId_ShouldThrowException() {
        // Given
        when(filmService.getFilmById(999L))
                .thenThrow(new NotFoundException("Фильм с id=999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.getFilm(999L));
        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }

    @Test
    void create_WithValidData_ShouldReturnFilm() {
        // Given
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New description");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(100);

        when(filmService.create(any(Film.class))).thenReturn(validFilm);

        // When
        Film result = filmController.create(newFilm);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(filmService, times(1)).create(newFilm);
    }

    @Test
    void create_WithInvalidData_ShouldThrowException() {
        // Given
        Film invalidFilm = new Film();
        invalidFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        when(filmService.create(any(Film.class)))
                .thenThrow(new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года"));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(invalidFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedFilm() {
        // Given
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        updatedFilm.setDuration(150);

        when(filmService.update(any(Film.class))).thenReturn(updatedFilm);

        // When
        Film result = filmController.update(updatedFilm);

        // Then
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(filmService, times(1)).update(updatedFilm);
    }

    @Test
    void update_WithNonExistentId_ShouldThrowException() {
        // Given
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(999L);
        nonExistentFilm.setName("Non Existent Film");

        when(filmService.update(any(Film.class)))
                .thenThrow(new NotFoundException("Фильм с id=999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.update(nonExistentFilm));
        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }

    @Test
    void addLike_ShouldCallService() {
        // When
        filmController.addLike(1L, 1L);

        // Then
        verify(filmService, times(1)).addLike(1L, 1L);
    }

    @Test
    void removeLike_ShouldCallService() {
        // When
        filmController.removeLike(1L, 1L);

        // Then
        verify(filmService, times(1)).removeLike(1L, 1L);
    }

    @Test
    void getPopularFilms_WithCustomCount_ShouldUseProvidedCount() {
        List<Film> popularFilms = Arrays.asList(validFilm);
        when(filmService.getPopularFilms(any(Integer.class))).thenReturn(popularFilms);
        List<Film> result = filmController.getPopularFilms(5);
        assertEquals(1, result.size());
        verify(filmService, times(1)).getPopularFilms(5);
    }
}