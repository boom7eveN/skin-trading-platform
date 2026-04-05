package com.skinmarket.marketplace.dto;

import com.skinmarket.marketplace.exception.ErrorCode;

public record MarketplaceErrorResponse(ErrorCode errorCode, String message, int status) {
}
