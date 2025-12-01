package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT id, name, description FROM ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getLong("id"), rs.getString("name"), rs.getString("description")));
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        String sql = "SELECT id, name, description FROM ratings WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getLong("id"), rs.getString("name"), rs.getString("description")), id);
        return mpaList.stream().findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}