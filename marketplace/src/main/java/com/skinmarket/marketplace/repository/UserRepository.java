package com.skinmarket.marketplace.repository;

import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.enums.UserRole;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, balance, role, version " +
                "FROM users WHERE username = :username";
        List<User> users = namedParameterJdbcTemplate.query(sql, Map.of("username", username), USER_ROW_MAPPER);
        return users.stream().findFirst();
    }

    public boolean createUser(User user) {
        String sql = """
                    INSERT INTO users(id, username, password_hash, balance, role, version)
                    VALUES (:id, :username, :passwordHash, :balance, :role, :version)
                """;
        return namedParameterJdbcTemplate.update(sql, Map.of(
                "id", user.id(),
                "username", user.username(),
                "passwordHash", user.passwordHash(),
                "balance", user.balance(),
                "role", user.role().name(),
                "version", user.version()
        )) == 1;
    }

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, _) -> new User(
            rs.getObject("id", UUID.class),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getBigDecimal("balance"),
            UserRole.valueOf(rs.getString("role")),
            rs.getLong("version")
    );
}