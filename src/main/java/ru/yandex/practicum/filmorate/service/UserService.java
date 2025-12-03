package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        log.info("Создание пользователя с email: {}", user.getEmail());

        boolean emailExists = userStorage.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Этот email уже используется.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());

        User existingUser = getUserById(user.getId());

        boolean emailExists = userStorage.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new ValidationException("Этот email уже используется.");
        }

        user.setFriendships(existingUser.getFriendships());
        return userStorage.update(user);
    }

    public List<User> findAll() {
        log.info("Получение всех пользователей");
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья: пользователь {} добавляет пользователя {}", userId, friendId);

        User user = getUserById(userId);
        getUserById(friendId);

        boolean alreadyFriend = user.getFriendships().stream()
                .anyMatch(f -> f.getFriendId().equals(friendId));

        if (alreadyFriend) {
            throw new ValidationException("Пользователь уже в друзьях");
        }

        Friendship friendship = new Friendship(userId, friendId, FriendshipStatus.CONFIRMED, LocalDateTime.now());
        user.getFriendships().add(friendship);

        userStorage.update(user);

        log.info("Пользователь {} успешно добавил пользователя {} в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление из друзей: пользователь {} удаляет пользователя {}", userId, friendId);

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        boolean removed = user.getFriendships().removeIf(
                friendship -> friendship.getFriendId().equals(friendId)
        );

        if (removed) {
            userStorage.update(user);
            log.info("Пользователь {} успешно удалил пользователя {} из друзей", userId, friendId);
        } else {
            log.info("Дружба между пользователями {} и {} не найдена, но это OK", userId, friendId);
        }
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение друзей пользователя с ID: {}", userId);

        User user = getUserById(userId);

        return user.getFriendships().stream()
                .filter(Friendship::isConfirmed) // Только подтвержденные друзья
                .map(friendship -> getUserById(friendship.getFriendId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);

        Set<Long> userFriends = getFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<Long> otherFriends = getFriends(otherId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        userFriends.retainAll(otherFriends);

        log.info("Найдено {} общих друзей", userFriends.size());

        return userFriends.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public void confirmFriendship(Long userId, Long friendId) {
        log.info("Подтверждение дружбы: пользователь {} подтверждает дружбу с {}", userId, friendId);

        User user = getUserById(userId);

        if (!user.hasPendingRequestTo(friendId)) {
            throw new ValidationException("Запрос на дружбу не найден");
        }

        user.confirmFriendship(friendId);
        userStorage.update(user);

        log.info("Дружба между пользователями {} и {} подтверждена", userId, friendId);
    }

    public List<User> getPendingFriendRequests(Long userId) {
        log.info("Получение входящих запросов дружбы для пользователя {}", userId);

        User user = getUserById(userId);
        return user.getPendingFriendRequests().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getSentFriendRequests(Long userId) {
        log.info("Получение исходящих запросов дружбы от пользователя {}", userId);

        return userStorage.findAll().stream()
                .filter(u -> u.hasPendingRequestTo(userId))
                .collect(Collectors.toList());
    }
}