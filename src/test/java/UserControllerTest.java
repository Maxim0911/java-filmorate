

import exceptions.NotFoundException;
import exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setId(1L);
        validUser.setEmail("test@mail.ru");
        validUser.setLogin("validlogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@mail.ru");
        List<User> users = Arrays.asList(validUser, user2);
        when(userService.findAll()).thenReturn(users);

        // When
        List<User> result = userController.findAll();

        // Then
        assertEquals(2, result.size());
        verify(userService, times(1)).findAll();
    }

    @Test
    void getUser_WithValidId_ShouldReturnUser() {
        // Given
        when(userService.getUserById(1L)).thenReturn(validUser);

        // When
        User result = userController.getUser(1L);

        // Then
        assertNotNull(result);
        assertEquals("test@mail.ru", result.getEmail());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.getUser(999L));
        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void create_WithValidData_ShouldReturnUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@mail.ru");
        newUser.setLogin("newlogin");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1995, 1, 1));

        when(userService.create(any(User.class))).thenReturn(validUser);

        // When
        User result = userController.create(newUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userService, times(1)).create(newUser);
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowException() {
        // Given
        User duplicateUser = new User();
        duplicateUser.setEmail("test@mail.ru");
        duplicateUser.setLogin("differentlogin");

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Этот email уже используется."));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(duplicateUser));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@mail.ru");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated User");

        when(userService.update(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userController.update(updatedUser);

        // Then
        assertEquals("updated@mail.ru", result.getEmail());
        assertEquals("updatedlogin", result.getLogin());
        verify(userService, times(1)).update(updatedUser);
    }

    @Test
    void update_WithNonExistentId_ShouldThrowException() {
        // Given
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("test@mail.ru");

        when(userService.update(any(User.class)))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.update(nonExistentUser));
        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    void addFriend_ShouldCallService() {
        // When
        userController.addFriend(1L, 2L);

        // Then
        verify(userService, times(1)).addFriend(1L, 2L);
    }

    @Test
    void removeFriend_ShouldCallService() {
        // When
        userController.removeFriend(1L, 2L);

        // Then
        verify(userService, times(1)).removeFriend(1L, 2L);
    }

    @Test
    void getFriends_ShouldReturnFriendsList() {
        // Given
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@mail.ru");
        List<User> friends = Arrays.asList(friend);
        when(userService.getFriends(1L)).thenReturn(friends);

        // When
        List<User> result = userController.getFriends(1L);

        // Then
        assertEquals(1, result.size());
        verify(userService, times(1)).getFriends(1L);
    }

    @Test
    void getCommonFriends_ShouldReturnCommonFriends() {
        // Given
        User commonFriend = new User();
        commonFriend.setId(3L);
        commonFriend.setEmail("common@mail.ru");
        List<User> commonFriends = Arrays.asList(commonFriend);
        when(userService.getCommonFriends(1L, 2L)).thenReturn(commonFriends);

        // When
        List<User> result = userController.getCommonFriends(1L, 2L);

        // Then
        assertEquals(1, result.size());
        verify(userService, times(1)).getCommonFriends(1L, 2L);
    }
}