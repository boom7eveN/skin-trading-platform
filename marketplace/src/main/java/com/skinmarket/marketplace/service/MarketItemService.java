package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.marketitem.CreateMarketItemRequest;
import com.skinmarket.marketplace.dto.marketitem.MarketItemResponse;
import com.skinmarket.marketplace.dto.pagination.PaginationResult;
import com.skinmarket.marketplace.entity.MarketItem;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.enums.MarketItemStatus;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.MarketItemMapper;
import com.skinmarket.marketplace.repository.MarketItemRepository;
import com.skinmarket.marketplace.repository.SkinRepository;
import com.skinmarket.marketplace.repository.UserRepository;
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


    public MarketItemService(
            MarketItemRepository marketItemRepository,
            SkinRepository skinRepository,
            UserRepository userRepository) {
        this.marketItemRepository = marketItemRepository;
        this.skinRepository = skinRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MarketItemResponse createMarketItem(
            UUID sellerId,
            CreateMarketItemRequest createMarketItemRequest) {



        var maybeSkin = skinRepository.findSkinById(createMarketItemRequest.skinId())
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.SKIN_NOT_FOUND,
                        String.format("Skin with id %s not found", createMarketItemRequest.skinId())
                ));

        MarketItem newMarketItem = MarketItemMapper.toEntity(
                sellerId,
                maybeSkin.id(),
                createMarketItemRequest
        );

        if (!marketItemRepository.createMarketItem(newMarketItem)) {
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    "Failed to create Market Item"
            );
        }

        return MarketItemMapper.toResponse(newMarketItem);

    }

    @Transactional
    public MarketItemResponse purchaseMarketItem(UUID marketItemId, UUID buyerId) {

        MarketItem marketItem = marketItemRepository.findMarketItemByIdWithPessimisticLock(marketItemId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.MARKET_ITEM_NOT_FOUND,
                        String.format("Market item with id %s not found", marketItemId)));

        if (marketItem.status() != MarketItemStatus.ACTIVE) {
            throw new BusinessLogicException(
                    ErrorCode.MARKET_ITEM_NOT_ACTIVE,
                    String.format("Market item with id %s is not active", marketItemId));
        }

        UUID sellerId = marketItem.sellerId();


        if (sellerId.equals(buyerId)) {
            throw new BusinessLogicException(
                    ErrorCode.CANNOT_BUY_OWN_ITEM,
                    String.format("User %s cannot purchase their own item %s", buyerId, marketItemId)
            );
        }

        User buyer = userRepository.findUserById(buyerId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("Buyer with id %s not found", buyerId)));

        User seller = userRepository.findUserById(sellerId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("Seller with id %s not found", sellerId)));

        if (buyer.balance().compareTo(marketItem.price()) < 0) {
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
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    String.format("Failed to update seller balance. Try again. User id: %s",
                            sellerId)
            );
        }


        marketItemRepository.updateMarketItemStatusToSold(
                marketItemId,
                LocalDateTime.now()
        );

        MarketItem updatedMarketItem = marketItemRepository.findMarketItemById(marketItemId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.MARKET_ITEM_NOT_FOUND,
                        String.format("Updated market item with id %s not found", marketItemId)
                ));

        return MarketItemMapper.toResponse(updatedMarketItem);
    }

    @Transactional
    public PaginationResult<MarketItemResponse> findAllMarketItemsWithPagination(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        PaginationResult<MarketItem> paginationResult = marketItemRepository.findAllMarketItemsWithPagination(page, size);

        List<MarketItemResponse> responses = MarketItemMapper.toListResponses(paginationResult.items());

        return new PaginationResult<>(
                responses,
                paginationResult.page(),
                paginationResult.size(),
                paginationResult.totalElements()
        );
    }
}
