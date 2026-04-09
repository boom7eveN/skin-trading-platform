package com.skinmarket.marketplace.controller;

import com.skinmarket.marketplace.dto.marketitem.CreateMarketItemRequest;
import com.skinmarket.marketplace.dto.marketitem.MarketItemResponse;
import com.skinmarket.marketplace.dto.pagination.PaginationResult;
import com.skinmarket.marketplace.security.UserDetailsImpl;
import com.skinmarket.marketplace.service.MarketItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/market-item")
public class MarketItemController {
    private final MarketItemService marketItemService;

    public MarketItemController(MarketItemService marketItemService) {
        this.marketItemService = marketItemService;
    }

    @PostMapping
    public ResponseEntity<MarketItemResponse> createMarketItem(
            @RequestBody CreateMarketItemRequest createMarketItemRequest,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        UUID sellerId = currentUser.getUserId();
        System.out.println(sellerId);
        MarketItemResponse response = marketItemService.createMarketItem(sellerId, createMarketItemRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<MarketItemResponse> purchaseMarketItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        UUID buyerId = currentUser.getUserId();
        MarketItemResponse response = marketItemService.purchaseMarketItem(id, buyerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paged")
    public ResponseEntity<PaginationResult<MarketItemResponse>> getMarketItemsWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaginationResult<MarketItemResponse> result = marketItemService.findAllMarketItemsWithPagination(page, size);
        return ResponseEntity.ok(result);
    }
}
