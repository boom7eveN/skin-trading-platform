package com.skinmarket.marketplace.repository;

import com.skinmarket.marketplace.dto.pagination.PaginationResult;
import com.skinmarket.marketplace.entity.MarketItem;
import com.skinmarket.marketplace.enums.MarketItemStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MarketItemRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public MarketItemRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean createMarketItem(MarketItem item) {
        String sql = """
                    INSERT INTO market_items(id, seller_id, skin_id, price, status, created_at, version)
                    VALUES (:id, :sellerId, :skinId, :price, :status, :createdAt, :version)
                """;
        return jdbc.update(sql, Map.of(
                "id", item.id(),
                "sellerId", item.sellerId(),
                "skinId", item.skinId(),
                "price", item.price(),
                "status", item.status().name(),
                "createdAt", item.createdAt(),
                "version", item.version()
        )) == 1;
    }

    public Optional<MarketItem> findMarketItemById(UUID id) {
        String sql = "SELECT id, seller_id, skin_id, price, status, created_at, sold_at, version " +
                "FROM market_items WHERE id = :id";
        List<MarketItem> items = jdbc.query(sql, Map.of("id", id), MARKET_ITEM_MAPPER);
        return items.stream().findFirst();
    }

    public Optional<MarketItem> findMarketItemByIdWithPessimisticLock(UUID id) {
        String sql = "SELECT id, seller_id, skin_id, price, status, created_at, sold_at, version " +
                "FROM market_items WHERE id = :id FOR UPDATE";
        List<MarketItem> items = jdbc.query(sql, Map.of("id", id), MARKET_ITEM_MAPPER);
        return items.stream().findFirst();
    }

    public boolean updateMarketItemStatusToSoldById(UUID id, LocalDateTime soldAt) {
        String sql = """
            UPDATE market_items
            SET status = 'SOLD',
                sold_at = :soldAt
            WHERE id = :id
              AND status = 'ACTIVE'
            """;

        return jdbc.update(sql, Map.of(
                "id", id,
                "soldAt", soldAt
        )) ==1;

    }


    public PaginationResult<MarketItem> findAllMarketItemsWithPagination(int page, int size) {
        int offset = (page - 1) * size;

        String dataSql = """
                    SELECT id, seller_id, skin_id, price, status, created_at, sold_at, version
                    FROM market_items
                    ORDER BY created_at DESC
                    LIMIT :limit
                    OFFSET :offset
                """;

        String countSql = "SELECT COUNT(*) FROM market_items";

        Map<String, Object> params = Map.of(
                "limit", size,
                "offset", offset
        );

        List<MarketItem> content = jdbc.query(dataSql, params, MARKET_ITEM_MAPPER);
        Integer totalElements = jdbc.queryForObject(countSql, Map.of(), Integer.class);

        return new PaginationResult<>(content, page, size, totalElements);
    }

    private static final RowMapper<MarketItem> MARKET_ITEM_MAPPER = (rs, _) -> new MarketItem(
            rs.getObject("id", UUID.class),
            rs.getObject("seller_id", UUID.class),
            rs.getObject("skin_id", UUID.class),
            rs.getBigDecimal("price"),
            MarketItemStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("sold_at") != null
                    ? rs.getTimestamp("sold_at").toLocalDateTime()
                    : null,
            rs.getLong("version")
    );
}
