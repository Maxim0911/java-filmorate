package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // генерирует конструктор со всеми полями
public class MpaResponse {
    private final Integer id;
    private final String name;
}