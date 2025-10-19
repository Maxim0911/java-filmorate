import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("test@mail.ru");
        validUser.setLogin("testLogin");
        validUser.setName("Test Name");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUser_WithValidData_ShouldSuccess() {
        User result = userController.create(validUser);

        assertNotNull(result.getId());
        assertEquals("test@mail.ru", result.getEmail());
        assertEquals("testLogin", result.getLogin());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowException() {
        validUser.setEmail("");

        assertThrows(ValidationException.class, () -> userController.create(validUser));
    }

    @Test
    void createUser_WithEmailWithoutAt_ShouldThrowException() {
        validUser.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> userController.create(validUser));
    }

    @Test
    void createUser_WithEmptyLogin_ShouldThrowException() {
        validUser.setLogin("");

        assertThrows(ValidationException.class, () -> userController.create(validUser));
    }

    @Test
    void createUser_WithLoginWithSpaces_ShouldThrowException() {
        validUser.setLogin("login with spaces");

        assertThrows(ValidationException.class, () -> userController.create(validUser));
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowException() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(validUser));
    }

    @Test
    void createUser_WithNullName_ShouldSetLoginAsName() {
        validUser.setName(null);

        User result = userController.create(validUser);
        assertEquals("testLogin", result.getName());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        userController.create(validUser);
        User duplicateUser = new User();
        duplicateUser.setEmail("test@mail.ru");
        duplicateUser.setLogin("anotherLogin");
        duplicateUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(duplicateUser));
    }

    @Test
    void updateUser_WithInvalidId_ShouldThrowException() {
        User invalidUser = new User();
        invalidUser.setId(-1L);

        assertThrows(NotFoundException.class, () -> userController.update(invalidUser));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowException() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("new@mail.ru");
        nonExistentUser.setLogin("newLogin");

        assertThrows(NotFoundException.class, () -> userController.update(nonExistentUser));
    }
}