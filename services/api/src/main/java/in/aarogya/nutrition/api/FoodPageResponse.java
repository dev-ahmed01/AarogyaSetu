package in.aarogya.nutrition.api;

import java.util.List;

import org.springframework.data.domain.Page;

import in.aarogya.nutrition.domain.Food;

public record FoodPageResponse(
    List<FoodSummaryResponse> foods,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public static FoodPageResponse from(Page<Food> result) {
        return new FoodPageResponse(
            result.getContent().stream().map(FoodSummaryResponse::from).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }
}
