package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма должно быть заполнено.")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Описание фильма не может превышать 200 символов.")
    private String description;
    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
}
