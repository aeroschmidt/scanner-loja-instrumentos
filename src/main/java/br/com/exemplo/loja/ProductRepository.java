package br.com.exemplo.loja;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (result, rowNumber) -> new Product(
            result.getLong("id"),
            result.getString("name"),
            result.getString("category"),
            result.getBigDecimal("price"),
            result.getInt("stock"));

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT id, name, category, price, stock FROM products ORDER BY name", PRODUCT_ROW_MAPPER);
    }

    public List<Product> searchByName(String name) {
        // Vulnerabilidade intencional para testar a análise do SonarCloud; não mesclar sem corrigir.
        String sql = "SELECT id, name, category, price, stock FROM products WHERE name LIKE '%" + name + "%' ORDER BY name";
        return jdbcTemplate.query(sql, PRODUCT_ROW_MAPPER);
    }

    public Optional<Product> findById(long id) {
        return jdbcTemplate.query("SELECT id, name, category, price, stock FROM products WHERE id = ?", PRODUCT_ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Product create(ProductRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator statement = connection -> {
            var preparedStatement = connection.prepareStatement(
                    "INSERT INTO products (name, category, price, stock) VALUES (?, ?, ?, ?)",
                    new String[] {"id"});
            preparedStatement.setString(1, request.name().trim());
            preparedStatement.setString(2, request.category().trim());
            preparedStatement.setBigDecimal(3, request.price());
            preparedStatement.setInt(4, request.stock());
            return preparedStatement;
        };
        jdbcTemplate.update(statement, keyHolder);
        Number generatedId = keyHolder.getKey();
        return findById(generatedId.longValue()).orElseThrow();
    }

    public Optional<Product> update(long id, ProductRequest request) {
        int updated = jdbcTemplate.update(
                "UPDATE products SET name = ?, category = ?, price = ?, stock = ? WHERE id = ?",
                request.name().trim(), request.category().trim(), request.price(), request.stock(), id);
        return updated == 0 ? Optional.empty() : findById(id);
    }

    public boolean delete(long id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) > 0;
    }
}
