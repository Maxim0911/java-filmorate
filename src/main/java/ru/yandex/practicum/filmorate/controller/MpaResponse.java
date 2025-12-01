package ru.yandex.practicum.filmorate.controller;

import lombok.Data;

@Data
public class MpaResponse {
    private final Integer id;
    private final String name;

    public MpaResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}