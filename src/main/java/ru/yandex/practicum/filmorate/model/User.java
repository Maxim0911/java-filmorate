package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    private String login;
    private String email;
    private String name;
    private LocalDate birthday;
}
