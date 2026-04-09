package com.skinmarket.marketplace.mapper;

import com.skinmarket.marketplace.dto.skin.CreateSkinRequest;
import com.skinmarket.marketplace.dto.skin.SkinResponse;
import com.skinmarket.marketplace.dto.skin.UpdateSkinRequest;
import com.skinmarket.marketplace.entity.Skin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkinMapper {

    public static Skin toEntity(CreateSkinRequest request) {
        return new Skin(
                UUID.randomUUID(),
                request.name(),
                request.weaponType(),
                request.rarity(),
                request.basePrice(),
                0L
        );
    }

    public static Skin toEntity(UUID id, UpdateSkinRequest request, Long version) {
        return new Skin(
                id,
                request.name(),
                request.weaponType(),
                request.rarity(),
                request.basePrice(),
                version
        );
    }

    public static SkinResponse toResponse(Skin skin) {
        return new SkinResponse(
                skin.id(),
                skin.name(),
                skin.weaponType(),
                skin.rarity(),
                skin.basePrice()
        );
    }

    public static List<SkinResponse> toListResponses(List<Skin> skins) {
        return skins.stream()
                .map(SkinMapper::toResponse)
                .collect(Collectors.toList());
    }
}