package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        Set<Friendship> friendships = loadFriendships(user.getId());
        user.setFriendships(friendships);

        return user;
    };

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());

        updateFriendships(user);

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updated == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }

        updateFriendships(user);

        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private Set<Friendship> loadFriendships(Long userId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ?";
        List<Friendship> friendships = jdbcTemplate.query(sql, friendshipRowMapper, userId);
        return new HashSet<>(friendships);
    }

    private void updateFriendships(User user) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        if (user.getFriendships() != null && !user.getFriendships().isEmpty()) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

            List<Object[]> batchArgs = new ArrayList<>();
            for (Friendship friendship : user.getFriendships()) {
                batchArgs.add(new Object[]{
                        user.getId(),
                        friendship.getFriendId(),
                        friendship.getStatus().name()
                });
            }

            jdbcTemplate.batchUpdate(insertSql, batchArgs);
        }
    }

    private final RowMapper<Friendship> friendshipRowMapper = (rs, rowNum) -> {
        Friendship friendship = new Friendship();
        friendship.setUserId(rs.getLong("user_id"));
        friendship.setFriendId(rs.getLong("friend_id"));
        friendship.setStatus(FriendshipStatus.valueOf(rs.getString("status")));
        friendship.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        return friendship;
    };
}