package com.skinmarket.marketplace.mapper;

import com.skinmarket.marketplace.dto.marketitem.CreateMarketItemRequest;
import com.skinmarket.marketplace.dto.marketitem.MarketItemResponse;
import com.skinmarket.marketplace.entity.MarketItem;
import com.skinmarket.marketplace.enums.MarketItemStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarketItemMapper {
    public static MarketItem toEntity(
            UUID sellerId,
            UUID skinId,
            CreateMarketItemRequest request) {
        return new MarketItem(
                UUID.randomUUID(),
                sellerId,
                skinId,
                request.price(),
                MarketItemStatus.ACTIVE,
                LocalDateTime.now(),
                null,
                0L
        );
    }

    public static MarketItemResponse toResponse(MarketItem marketItem) {
        return new MarketItemResponse(
                marketItem.id(),
                marketItem.sellerId(),
                marketItem.skinId(),
                marketItem.price(),
                marketItem.status(),
                marketItem.createdAt(),
                marketItem.soldAt()
        );
    }

    public static List<MarketItemResponse> toListResponses(List<MarketItem> marketItems) {
        return marketItems.stream()
                .map(MarketItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}
