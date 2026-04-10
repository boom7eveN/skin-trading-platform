package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.marketitem.CreateMarketItemRequest;
import com.skinmarket.marketplace.dto.marketitem.MarketItemPurchasedEvent;
import com.skinmarket.marketplace.dto.marketitem.MarketItemResponse;
import com.skinmarket.marketplace.dto.pagination.PaginationResult;
import com.skinmarket.marketplace.entity.MarketItem;
import com.skinmarket.marketplace.entity.OutboxEvent;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.enums.MarketItemStatus;
import com.skinmarket.marketplace.enums.OutboxEventType;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.MarketItemMapper;
import com.skinmarket.marketplace.repository.MarketItemRepository;
import com.skinmarket.marketplace.repository.OutboxRepository;
import com.skinmarket.marketplace.repository.SkinRepository;
import com.skinmarket.marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MarketItemService {
    private final MarketItemRepository marketItemRepository;
    private final SkinRepository skinRepository;
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketItemService.class);


    public MarketItemService(
            MarketItemRepository marketItemRepository,
            SkinRepository skinRepository,
            UserRepository userRepository,
            OutboxRepository outboxRepository) {
        this.marketItemRepository = marketItemRepository;
        this.skinRepository = skinRepository;
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public MarketItemResponse createMarketItem(
            UUID sellerId,
            CreateMarketItemRequest createMarketItemRequest) {
        LOGGER.info("Creating market item for seller: {}, skin: {}, price: {}",
                sellerId, createMarketItemRequest.skinId(), createMarketItemRequest.price());

        var maybeSkin = skinRepository.findSkinById(createMarketItemRequest.skinId())
                .orElseThrow(() -> {
                    LOGGER.warn("Skin not found with id: {}", createMarketItemRequest.skinId());
                    return new BusinessLogicException(
                            ErrorCode.SKIN_NOT_FOUND,
                            String.format("Skin with id %s not found", createMarketItemRequest.skinId())
                    );
                });

        MarketItem newMarketItem = MarketItemMapper.toEntity(
                sellerId,
                maybeSkin.id(),
                createMarketItemRequest
        );

        if (!marketItemRepository.createMarketItem(newMarketItem)) {
            LOGGER.error("Failed to create market item in database for seller: {}, skin: {}",
                    sellerId, createMarketItemRequest.skinId());
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    "Failed to create Market Item"
            );
        }
        LOGGER.info("Market item created successfully: id={}, seller={}, skin={}, price={}",
                newMarketItem.id(), sellerId, newMarketItem.skinId(), newMarketItem.price());
        return MarketItemMapper.toResponse(newMarketItem);

    }

    @Transactional
    public MarketItemResponse purchaseMarketItem(UUID marketItemId, UUID buyerId) {

        LOGGER.info("Purchase attempt - marketItem: {}, buyer: {}", marketItemId, buyerId);

        MarketItem marketItem = marketItemRepository.findMarketItemByIdWithPessimisticLock(marketItemId)
                .orElseThrow(() -> {
                    LOGGER.warn("Market item not found with id: {}", marketItemId);
                    return new BusinessLogicException(
                            ErrorCode.MARKET_ITEM_NOT_FOUND,
                            String.format("Market item with id %s not found", marketItemId));
                });

        if (marketItem.status() != MarketItemStatus.ACTIVE) {
            LOGGER.warn("Purchase failed - market item not active: {}, status: {}",
                    marketItemId, marketItem.status());
            throw new BusinessLogicException(
                    ErrorCode.MARKET_ITEM_NOT_ACTIVE,
                    String.format("Market item with id %s is not active", marketItemId));
        }

        UUID sellerId = marketItem.sellerId();


        if (sellerId.equals(buyerId)) {
            LOGGER.warn("Purchase failed - user {} attempted to buy own item {}", buyerId, marketItemId);
            throw new BusinessLogicException(
                    ErrorCode.CANNOT_BUY_OWN_ITEM,
                    String.format("User %s cannot purchase their own item %s", buyerId, marketItemId)
            );
        }

        User buyer = userRepository.findUserById(buyerId)
                .orElseThrow(() -> {
                    LOGGER.error("Buyer not found: {}", buyerId);
                    return new BusinessLogicException(
                            ErrorCode.USER_NOT_FOUND,
                            String.format("Buyer with id %s not found", buyerId));
                });

        User seller = userRepository.findUserById(sellerId)
                .orElseThrow(() -> {
                    LOGGER.error("Seller not found: {}", sellerId);
                    return new BusinessLogicException(
                            ErrorCode.USER_NOT_FOUND,
                            String.format("Seller with id %s not found", sellerId));
                });


        if (buyer.balance().compareTo(marketItem.price()) < 0) {
            LOGGER.warn("Purchase failed - not enough money for buyer: {}, required: {}, available: {}",
                    buyerId, marketItem.price(), buyer.balance());
            throw new BusinessLogicException(ErrorCode.NOT_ENOUGH_MONEY,
                    String.format("Not enough money. Required: %s, Available: %s",
                            marketItem.price(), buyer.balance()));
        }

        Boolean buyerUpdated = userRepository.updateUserBalanceWithOptimisticLock(
                buyer.id(),
                buyer.balance().subtract(marketItem.price()),
                buyer.version()
        );

        if (!buyerUpdated) {
            LOGGER.warn("Optimistic lock failure when updating buyer balance: {}, version: {}",
                    buyerId, buyer.version());
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    String.format("Failed to update buyer balance. Try again. User id: %s",
                            buyerId)
            );
        }

        Boolean sellerUpdated = userRepository.updateUserBalanceWithOptimisticLock(
                seller.id(),
                seller.balance().add(marketItem.price()),
                seller.version()
        );

        if (!sellerUpdated) {
            LOGGER.warn("Optimistic lock failure when updating seller balance: {}, version: {}",
                    sellerId, seller.version());
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    String.format("Failed to update seller balance. Try again. User id: %s",
                            sellerId)
            );
        }


        boolean statusUpdated = marketItemRepository.updateMarketItemStatusToSoldById(
                marketItemId,
                LocalDateTime.now()
        );
        if (!statusUpdated) {
            LOGGER.error("Failed to update market item status to SOLD: {}", marketItemId);
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    String.format("Failed to update market item status to SOLD: %s", marketItemId)
            );
        }


        boolean outboxCreated = outboxRepository.createOutboxEvent(
                OutboxEvent.create(
                        marketItemId,
                        OutboxEventType.MARKET_ITEM_PURCHASED,
                        new MarketItemPurchasedEvent(
                                marketItemId, sellerId, buyerId,
                                marketItem.skinId(), marketItem.price(), marketItem.soldAt()
                        ).toJson()
                )
        );

        if (!outboxCreated) {
            LOGGER.error("Failed to create outbox event for purchase: marketItem={}, buyer={}, seller={}",
                    marketItemId, buyerId, sellerId);
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    String.format("Failed to create outbox event for market item purchase: %s", marketItemId)
            );
        }


        MarketItem updatedMarketItem = marketItemRepository.findMarketItemById(marketItemId)
                .orElseThrow(() -> {
                    LOGGER.error("Market item not found after purchase: {}", marketItemId);
                    return new BusinessLogicException(
                            ErrorCode.MARKET_ITEM_NOT_FOUND,
                            String.format("Updated market item with id %s not found", marketItemId));
                });
        LOGGER.info("Purchase completed successfully - marketItem: {}, buyer: {}, seller: {}, price: {}, " +
                        "transaction time: {}",
                marketItemId, buyerId, sellerId, updatedMarketItem.price(), updatedMarketItem.soldAt());
        return MarketItemMapper.toResponse(updatedMarketItem);
    }

    @Transactional
    public PaginationResult<MarketItemResponse> findAllMarketItemsWithPagination(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        PaginationResult<MarketItem> paginationResult =
                marketItemRepository.findAllMarketItemsWithPagination(page, size);

        List<MarketItemResponse> responses = MarketItemMapper.toListResponses(paginationResult.items());

        return new PaginationResult<>(
                responses,
                paginationResult.page(),
                paginationResult.size(),
                paginationResult.totalElements()
        );
    }
}
