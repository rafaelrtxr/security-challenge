package com.yieldstreet.challenges.security.document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentDao {
    private final JdbcTemplate jdbcTemplate;
    private final DocumentRowMapper documentRowMapper;
    private final DocumentSummaryRowMapper documentSummaryRowMapper;

    public DocumentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.documentRowMapper = new DocumentRowMapper();
        this.documentSummaryRowMapper = new DocumentSummaryRowMapper();
    }

    public void insert(Document document) {
        //FIXME: SQL Injection #6
        jdbcTemplate.update("insert into document (id, user_id, name, contents) values ('"
                + document.id() + "', '"
                + document.userId() + "', '"
                + document.name() + "', x'"
                + HexFormat.of().formatHex(document.contents()) + "')");
    }

    public Document findById(String id) {
        //FIXME: SQL Injection #7
        return jdbcTemplate.queryForObject("select * from document where id = '" + id + "'", documentRowMapper);
    }

    public List<DocumentSummary> findByUserId(String userId, int page, int pageSize) {
        int offset = page * pageSize;
        String query = "SELECT * FROM document WHERE user_id = ? LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query, documentSummaryRowMapper, userId, pageSize, offset);
    }

    public int countByUserId(String userId){
        String query = "SELECT COUNT(*) FROM document WHERE user_id = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, userId);    
    } 

    private static class DocumentRowMapper implements RowMapper<Document> {
        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Document(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("user_id")),
                    rs.getString("name"),
                    rs.getBytes("contents"));
        }
    }

    private static class DocumentSummaryRowMapper implements RowMapper<DocumentSummary> {
        @Override
        public DocumentSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DocumentSummary(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"));
        }
    }
}
