package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;
    private String name;

    @PastOrPresent(message = "Некорректно введена дата рождения. Вы еще не родились:)")
    private LocalDate birthday;

    private Set<Friendship> friendships = new HashSet<>();

    public User() {
        this.friendships = new HashSet<>();
    }

    public User(Long id, String login, String email, String name, LocalDate birthday) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.name = name;
        this.birthday = birthday;
        this.friendships = new HashSet<>();
    }

    public Set<Long> getPendingFriendRequests() {
        Set<Long> pendingIds = new HashSet<>();
        for (Friendship friendship : friendships) {
            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                pendingIds.add(friendship.getFriendId());
            }
        }
        return pendingIds;
    }

    public void addFriendRequest(Long friendId) {
        Friendship friendship = new Friendship(this.id, friendId, FriendshipStatus.PENDING, LocalDateTime.now());
        this.friendships.add(friendship);
    }

    public void confirmFriendship(Long friendId) {
        for (Friendship friendship : friendships) {
            if (friendship.getFriendId().equals(friendId)) {
                friendship.confirm();
                break;
            }
        }
    }

    public void removeFriendship(Long friendId) {
        this.friendships.removeIf(friendship -> friendship.getFriendId().equals(friendId));
    }

    public boolean isFriend(Long friendId) {
        return friendships.stream()
                .anyMatch(friendship -> friendship.getFriendId().equals(friendId) && friendship.isConfirmed());
    }

    public boolean hasPendingRequestTo(Long friendId) {
        return friendships.stream()
                .anyMatch(friendship -> friendship.getFriendId().equals(friendId) &&
                        friendship.getStatus() == FriendshipStatus.PENDING);
    }

    public Set<Long> getConfirmedFriendIds() {
        Set<Long> friendIds = new HashSet<>();
        for (Friendship friendship : friendships) {
            if (friendship.isConfirmed()) {
                friendIds.add(friendship.getFriendId());
            }
        }
        return friendIds;
    }
}