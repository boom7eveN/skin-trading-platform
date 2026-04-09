package com.skinmarket.marketplace.exception;

import com.skinmarket.marketplace.dto.error.MarketplaceErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<MarketplaceErrorResponse> handleBusinessLogicException(BusinessLogicException ex) {

        HttpStatus status = mapToHttpStatus(ex.getErrorCode());

        MarketplaceErrorResponse error = new MarketplaceErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                status.value()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MarketplaceErrorResponse> handleUnexpectedException(final Exception exception) {

        MarketplaceErrorResponse error = new MarketplaceErrorResponse(
                ErrorCode.UNEXPECTED_ERROR,
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private HttpStatus mapToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case SKIN_NOT_FOUND, USER_NOT_FOUND, MARKET_ITEM_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case SKIN_ALREADY_EXISTS, USER_ALREADY_EXISTS, OPTIMISTIC_LOCK_FAILURE -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR, CANNOT_BUY_OWN_ITEM -> HttpStatus.BAD_REQUEST;
            case NOT_ENOUGH_MONEY -> HttpStatus.PAYMENT_REQUIRED;
            case MARKET_ITEM_NOT_ACTIVE -> HttpStatus.GONE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }


}
