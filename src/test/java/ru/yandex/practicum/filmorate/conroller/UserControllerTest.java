package ru.yandex.practicum.filmorate.conroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        testUser2 = new User();
        testUser2.setEmail("test2@mail.ru");
        testUser2.setLogin("testlogin2");
        testUser2.setName("Test User 2");
        testUser2.setBirthday(LocalDate.of(1995, 1, 1));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        // Act
        ResponseEntity<User> response = restTemplate.postForEntity("/users", testUser, User.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
        assertEquals(testUser.getLogin(), response.getBody().getLogin());
        assertEquals(testUser.getName(), response.getBody().getName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() {
        // Arrange
        User userWithEmptyName = new User();
        userWithEmptyName.setEmail("noname@mail.ru");
        userWithEmptyName.setLogin("nologin");
        userWithEmptyName.setName("");
        userWithEmptyName.setBirthday(LocalDate.of(1990, 1, 1));

        // Act
        ResponseEntity<User> response = restTemplate.postForEntity("/users", userWithEmptyName, User.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userWithEmptyName.getLogin(), response.getBody().getName());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        restTemplate.postForEntity("/users", testUser, User.class);
        restTemplate.postForEntity("/users", testUser2, User.class);

        // Act
        ResponseEntity<List<User>> response = restTemplate.exchange(
                "/users",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUser_ShouldReturnUserById() {
        // Arrange
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", testUser, User.class);
        Long userId = createResponse.getBody().getId();

        // Act
        ResponseEntity<User> response = restTemplate.getForEntity("/users/" + userId, User.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        // Arrange
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", testUser, User.class);
        User createdUser = createResponse.getBody();

        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("updated@mail.ru");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        // Act
        HttpEntity<User> request = new HttpEntity<>(updatedUser);
        ResponseEntity<User> response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                request,
                User.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updated@mail.ru", response.getBody().getEmail());
        assertEquals("updatedlogin", response.getBody().getLogin());
        assertEquals("Updated Name", response.getBody().getName());
    }

    @Test
    void addAndRemoveFriend_ShouldWorkCorrectly() {
        // Arrange
        ResponseEntity<User> user1Response = restTemplate.postForEntity("/users", testUser, User.class);
        ResponseEntity<User> user2Response = restTemplate.postForEntity("/users", testUser2, User.class);

        Long user1Id = user1Response.getBody().getId();
        Long user2Id = user2Response.getBody().getId();

        // Act - Добавляем друга
        restTemplate.put("/users/" + user1Id + "/friends/" + user2Id, null);

        // Assert - Проверяем друзей
        ResponseEntity<List<User>> friendsResponse = restTemplate.exchange(
                "/users/" + user1Id + "/friends",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );

        assertEquals(HttpStatus.OK, friendsResponse.getStatusCode());
        assertNotNull(friendsResponse.getBody());

        // Act - Удаляем друга
        restTemplate.delete("/users/" + user1Id + "/friends/" + user2Id);

        // Assert - Проверяем друзей после удаления
        ResponseEntity<List<User>> friendsAfterRemoval = restTemplate.exchange(
                "/users/" + user1Id + "/friends",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );

        assertEquals(HttpStatus.OK, friendsAfterRemoval.getStatusCode());
        assertNotNull(friendsAfterRemoval.getBody());
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends() {
        // Arrange
        User user3 = new User();
        user3.setEmail("user3@mail.ru");
        user3.setLogin("user3login");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1992, 1, 1));

        ResponseEntity<User> user1Response = restTemplate.postForEntity("/users", testUser, User.class);
        ResponseEntity<User> user2Response = restTemplate.postForEntity("/users", testUser2, User.class);
        ResponseEntity<User> user3Response = restTemplate.postForEntity("/users", user3, User.class);

        Long user1Id = user1Response.getBody().getId();
        Long user2Id = user2Response.getBody().getId();
        Long user3Id = user3Response.getBody().getId();

        // Добавляем общего друга
        restTemplate.put("/users/" + user1Id + "/friends/" + user3Id, null);
        restTemplate.put("/users/" + user2Id + "/friends/" + user3Id, null);

        // Act
        ResponseEntity<List<User>> commonFriendsResponse = restTemplate.exchange(
                "/users/" + user1Id + "/friends/common/" + user2Id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, commonFriendsResponse.getStatusCode());
        assertNotNull(commonFriendsResponse.getBody());
    }

    @Test
    void getUser_WithInvalidId_ShouldReturnNotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity("/users/9999", String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}