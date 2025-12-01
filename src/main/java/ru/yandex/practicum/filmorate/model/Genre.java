package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    private Long id;
    private String name;

    public Genre(Long id) {
        this.id = id;
        this.name = null; // Имя будет заполнено позже
    }
}