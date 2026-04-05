package com.skinmarket.marketplace.dto;

import java.util.List;

public record PaginationResult<T>(List<T> items, int page, int size, int totalElements) {
}
