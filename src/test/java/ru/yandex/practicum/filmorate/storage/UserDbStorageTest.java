package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(UserDbStorage.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;

    @BeforeEach
    void setUp() {
        // Очистка базы данных в правильном порядке (сначала связи, потом пользователи)
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM users");
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void create_ShouldCreateUserSuccessfully() {
        // Given
        User user = createTestUser();

        // When
        User createdUser = userDbStorage.create(user);

        // Then
        assertNotNull(createdUser);
        assertThat(createdUser.getId()).isPositive();
        assertEquals("test@mail.ru", createdUser.getEmail());
        assertEquals("testlogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    void findById_ShouldReturnUserWhenExists() {
        // Given
        User user = createTestUser();
        User createdUser = userDbStorage.create(user);
        Long userId = createdUser.getId();

        // When
        Optional<User> foundUser = userDbStorage.findById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@mail.ru", foundUser.get().getEmail());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("Test User", foundUser.get().getName());
    }

    @Test
    void findById_ShouldReturnEmptyOptionalWhenUserNotFound() {
        // When
        Optional<User> foundUser = userDbStorage.findById(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        User user1 = createTestUser();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        userDbStorage.create(user1);

        User user2 = createTestUser();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        userDbStorage.create(user2);

        // When
        List<User> users = userDbStorage.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@mail.ru", "user2@mail.ru");
    }

    @Test
    void update_ShouldUpdateUserSuccessfully() {
        // Given
        User user = createTestUser();
        User createdUser = userDbStorage.create(user);
        Long userId = createdUser.getId();

        User userToUpdate = createTestUser();
        userToUpdate.setId(userId);
        userToUpdate.setEmail("updated@mail.ru");
        userToUpdate.setLogin("updatedlogin");
        userToUpdate.setName("Updated User");

        // When
        User updatedUser = userDbStorage.update(userToUpdate);

        // Then
        assertNotNull(updatedUser);
        assertEquals("updated@mail.ru", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Updated User", updatedUser.getName());

        // Verify in database
        Optional<User> foundUser = userDbStorage.findById(userId);
        assertTrue(foundUser.isPresent());
        assertEquals("updated@mail.ru", foundUser.get().getEmail());
    }

    @Test
    void delete_ShouldDeleteUserSuccessfully() {
        // Given
        User user = createTestUser();
        User createdUser = userDbStorage.create(user);
        Long userId = createdUser.getId();

        // When
        userDbStorage.delete(userId);

        // Then
        Optional<User> deletedUser = userDbStorage.findById(userId);
        assertFalse(deletedUser.isPresent());
    }
}