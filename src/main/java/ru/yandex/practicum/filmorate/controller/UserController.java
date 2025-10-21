package ru.yandex.practicum.filmorate.controller;

import exceptions.NotFoundException;
import exceptions.ValidationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated

public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

        try {
            log.info("Добавлен новый пользователь с email={}", user.getEmail());

            boolean existsEmail = users.values().stream()
                    .anyMatch(e -> e.getEmail().equals(user.getEmail()));

            if (existsEmail) {
                throw new ValidationException("Этот email уже используется.");
            }

            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            user.setId(getNextId());
            users.put(user.getId(), user);

            log.info("Новый пользователь успешно добавлен с ID={}", user.getId());

            return user;
        } catch (ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        try {
            if (newUser.getId() <= 0) {
                throw new NotFoundException("Необходимо указать ID пользователя");
            }

            if (!users.containsKey(newUser.getId())) {
                throw new NotFoundException(format("Пользователь с id=%d не найден", newUser.getId()));
            }
            log.info("Данные пользователя с ID={}", newUser.getId() + " обновлены.");

            User oldUser = users.get(newUser.getId());

            if (newUser.getEmail() != null) {
                boolean existsEmail = users.values().stream()
                        .anyMatch(e -> e.getEmail().equals(newUser.getEmail()));

                if (existsEmail) {
                    throw new ValidationException("Этот email уже используется.");
                }

                if (!newUser.getEmail().contains("@")) {
                    throw new ValidationException("Email должен содержать символ @");
                }
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null) {
                if (newUser.getLogin().contains(" ")) {
                throw new ValidationException("Логин не должен содержать пробелы.");
            }
                oldUser.setLogin(newUser.getLogin());
        }

            if (newUser.getBirthday() != null) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    throw new ValidationException("Некорректно введена дата рождения. Вы еще не родились:)");
                }
                oldUser.setBirthday(newUser.getBirthday());
            }

            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            log.info("Данные пользователя с ID={}", newUser.getId() + " обновлены.");
            return oldUser;
    } catch (NotFoundException | ValidationException e) {
            log.error(e.getMessage());
            throw e;
        }
}

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return currentMaxId + 1;
    }
}