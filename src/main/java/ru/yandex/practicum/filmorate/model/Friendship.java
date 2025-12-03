package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private LocalDateTime createdDate = LocalDateTime.now(); // инициализация по умолчанию

    public boolean isConfirmed() {
        return this.status == FriendshipStatus.CONFIRMED;
    }

    public void confirm() {
        this.status = FriendshipStatus.CONFIRMED;
    }
}