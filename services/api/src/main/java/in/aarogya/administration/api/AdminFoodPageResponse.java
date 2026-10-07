package in.aarogya.administration.api;

import java.util.List;

public record AdminFoodPageResponse(
    List<AdminFoodSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
