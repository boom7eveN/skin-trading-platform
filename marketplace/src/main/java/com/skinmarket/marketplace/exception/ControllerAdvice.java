package com.skinmarket.marketplace.exception;

import com.skinmarket.marketplace.dto.MarketplaceErrorResponse;
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

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }


}
