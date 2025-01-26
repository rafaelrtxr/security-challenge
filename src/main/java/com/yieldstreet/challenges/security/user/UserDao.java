package com.yieldstreet.challenges.security.user;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = MessageFormat.format("insert into user (id, email, name, password) " +
                "values (''{0}'', ''{1}'', ''{2}'', ''{3}'')", user.id(), user.email(), user.name(), user.password());
        //FIXME: SQL Injection #1 (https://cwe.mitre.org/data/definitions/89)
        jdbcTemplate.execute(sql);
    }

    public UUID authenticate(String email, String password) {
        //FIXME: SQL Injection #2 (https://cwe.mitre.org/data/definitions/89)
        String sql = "select id from user where email = '" + email + "' AND password = '" + password + "';";
        var userIds = jdbcTemplate.queryForList(sql, UUID.class);
        return userIds.isEmpty() ? null : userIds.get(0);
    }

    public void changePassword(String id, String password) {
        //FIXME: SQL Injection #3 (https://cwe.mitre.org/data/definitions/89)
        String sql = "update user set password = '" + password + "' where id = '" + id + "';";
        jdbcTemplate.execute(sql);
    }

}
