
package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя с ID={}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя с email={}", user.getEmail());
        User createdUser = userService.create(user);
        log.info("Пользователь успешно добавлен с ID={}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с ID={}", user.getId());
        User updatedUser = userService.update(user);
        log.info("Пользователь с ID={} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление в друзья: пользователь ID={} добавляет пользователя ID={}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователи успешно добавлены в друзья");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь ID={} удаляет пользователя ID={}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователи успешно удалены из друзей");
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение друзей пользователя ID={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей пользователей ID={} и ID={}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}