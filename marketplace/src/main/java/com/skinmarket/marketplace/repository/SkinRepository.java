package com.skinmarket.marketplace.repository;

import com.skinmarket.marketplace.dto.pagination.PaginationResult;
import com.skinmarket.marketplace.entity.Skin;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SkinRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SkinRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Skin> findSkinById(UUID id) {
        String sql = "SELECT id, name, weapon_type, rarity, base_price, version FROM skins WHERE id = :id;";
        List<Skin> skins = namedParameterJdbcTemplate.query(sql, Map.of("id", id), SKIN_ROW_MAPPER);
        return skins.stream().findFirst();
    }

    public List<Skin> findAllSkins() {
        String sql = "SELECT id, name, weapon_type, rarity, base_price, version FROM skins;";
        return namedParameterJdbcTemplate.query(sql, SKIN_ROW_MAPPER);
    }

    public Optional<Skin> findSkinByNameAndWeaponTypeAndRarity(String name, String weaponType, String rarity) {
        String sql = "SELECT id, name, weapon_type, rarity, base_price, version FROM skins " +
                "WHERE name = :name AND weapon_type = :weaponType AND rarity = :rarity;";
        Map<String, Object> params = Map.of(
                "name", name,
                "weaponType", weaponType,
                "rarity", rarity
        );
        List<Skin> skins = namedParameterJdbcTemplate.query(sql, params, SKIN_ROW_MAPPER);
        return skins.stream().findFirst();
    }

    public boolean createSkin(Skin skin) {

        String sql = "INSERT INTO skins(id, name, weapon_type, rarity, base_price, version) " +
                "VALUES (:id, :name, :weaponType, :rarity, :basePrice, :version);";
        return namedParameterJdbcTemplate.update(
                sql, Map.of(
                        "id", skin.id(),
                        "name", skin.name(),
                        "weaponType", skin.weaponType(),
                        "rarity", skin.rarity(),
                        "basePrice", skin.basePrice(),
                        "version", skin.version()
                )
        ) == 1;
    }

    public boolean updateSkinWithOptimisticLock(Skin skin) {
        String sql = """
                UPDATE skins
                SET name = :name,
                    weapon_type = :weaponType,
                    rarity = :rarity,
                    base_price = :basePrice,
                    version = :newVersion
                WHERE id = :id AND version = :oldVersion
                """;

        return namedParameterJdbcTemplate.update(
                sql, Map.of(
                        "id", skin.id(),
                        "name", skin.name(),
                        "weaponType", skin.weaponType(),
                        "rarity", skin.rarity(),
                        "basePrice", skin.basePrice(),
                        "newVersion", skin.version() + 1,
                        "oldVersion", skin.version()
                )
        ) == 1;
    }


    public boolean deleteSkinById(UUID id) {
        String sql = "DELETE FROM skins WHERE id = :id;";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id)) == 1;
    }


    public PaginationResult<Skin> findAllSkinsWithPagination(int page, int size) {
        int offset = (page - 1) * size;

        String dataSql = """
                    SELECT id, name, weapon_type, rarity, base_price, version
                    FROM skins
                    ORDER BY name
                    LIMIT :limit
                    OFFSET :offset
                """;

        String countSql = "SELECT COUNT(*) FROM skins";

        Map<String, Object> params = Map.of(
                "limit", size,
                "offset", offset
        );

        List<Skin> content = namedParameterJdbcTemplate.query(dataSql, params, SKIN_ROW_MAPPER);
        Integer totalElements = namedParameterJdbcTemplate.queryForObject(countSql, Map.of(), Integer.class);

        return new PaginationResult<>(content, page, size, totalElements);
    }


    private static final RowMapper<Skin> SKIN_ROW_MAPPER = (rs, _) -> new Skin(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("weapon_type"),
            rs.getString("rarity"),
            rs.getBigDecimal("base_price"),
            rs.getLong("version")
    );
}
