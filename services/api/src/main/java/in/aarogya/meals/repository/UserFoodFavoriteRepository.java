package in.aarogya.meals.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.meals.domain.UserFoodFavorite;

public interface UserFoodFavoriteRepository
    extends JpaRepository<UserFoodFavorite, UUID> {

    List<UserFoodFavorite> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<UserFoodFavorite> findByUser_IdAndFood_Id(UUID userId, UUID foodId);
}
