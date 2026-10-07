package in.aarogya.administration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.administration.domain.FoodCurationReview;

public interface FoodCurationReviewRepository
    extends JpaRepository<FoodCurationReview, UUID> {

    List<FoodCurationReview> findByFood_IdOrderByOccurredAtDesc(UUID foodId);
}
