

import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("validlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createUser_WithValidData_ShouldSuccess() {
        User user = createValidUser();

        User result = userController.create(user);

        assertNotNull(result.getId());
        assertEquals("test@mail.ru", result.getEmail());
        assertEquals("validlogin", result.getLogin());
        assertEquals("Test User", result.getName());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        User user1 = createValidUser();
        userController.create(user1);

        User user2 = createValidUser();
        user2.setLogin("differentlogin");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user2));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void createUser_WithNullName_ShouldSetLoginAsName() {
        User user = createValidUser();
        user.setName(null);

        User result = userController.create(user);

        assertEquals("validlogin", result.getName());
    }

    @Test
    void createUser_WithBlankName_ShouldSetLoginAsName() {
        User user = createValidUser();
        user.setName("   ");

        User result = userController.create(user);

        assertEquals("validlogin", result.getName());
    }

    @Test
    void updateUser_WithValidData_ShouldSuccess() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("new@mail.ru");
        updateUser.setLogin("newlogin");
        updateUser.setName("New Name");
        updateUser.setBirthday(LocalDate.of(1991, 1, 1));

        User result = userController.update(updateUser);

        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals(LocalDate.of(1991, 1, 1), result.getBirthday());
    }

    @Test
    void updateUser_WithNegativeId_ShouldThrowException() {
        User updateUser = new User();
        updateUser.setId(-1L);
        updateUser.setEmail("test@mail.ru");
        updateUser.setLogin("testlogin");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.update(updateUser));
        assertEquals("Необходимо указать ID пользователя", exception.getMessage());
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowException() {
        // Создаем первого пользователя
        User user1 = createValidUser();
        user1.setEmail("user1@mail.ru");
        userController.create(user1);

        // Создаем второго пользователя
        User user2 = createValidUser();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        User createdUser2 = userController.create(user2);

        // Пытаемся обновить второго пользователя с email первого
        User updateUser = new User();
        updateUser.setId(createdUser2.getId());
        updateUser.setEmail("user1@mail.ru"); // дублируем email первого пользователя
        updateUser.setLogin("user2");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void updateUser_WithLoginContainingSpaces_ShouldThrowException() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setLogin("login with spaces");
        updateUser.setEmail("new@mail.ru"); // новый email чтобы избежать конфликта

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Логин не должен содержать пробелы.", exception.getMessage());
    }

    @Test
    void updateUser_WithFutureBirthday_ShouldThrowException() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setBirthday(LocalDate.now().plusDays(1));
        updateUser.setEmail("new@mail.ru"); // новый email чтобы избежать конфликта

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Некорректно введена дата рождения. Вы еще не родились:)", exception.getMessage());
    }

    @Test
    void updateUser_PartialUpdate_ShouldSuccess() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        User partialUpdate = new User();
        partialUpdate.setId(createdUser.getId());
        partialUpdate.setEmail("new@mail.ru");
        // остальные поля остаются null

        User result = userController.update(partialUpdate);

        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("validlogin", result.getLogin()); // старое значение
        assertEquals("Test User", result.getName()); // старое значение
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = createValidUser();
        userController.create(user1);

        User user2 = createValidUser();
        user2.setEmail("another@mail.ru");
        user2.setLogin("anotherlogin");
        userController.create(user2);

        Collection<User> users = userController.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyCollection() {
        Collection<User> users = userController.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void idGeneration_ShouldBeSequential() {
        User user1 = createValidUser();
        User createdUser1 = userController.create(user1);

        User user2 = createValidUser();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        User createdUser2 = userController.create(user2);

        assertEquals(1L, createdUser1.getId());
        assertEquals(2L, createdUser2.getId());
    }
}