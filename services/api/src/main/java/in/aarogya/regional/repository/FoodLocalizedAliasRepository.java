package in.aarogya.regional.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.regional.domain.FoodLocalizedAlias;

public interface FoodLocalizedAliasRepository
    extends JpaRepository<FoodLocalizedAlias, UUID> {

    List<FoodLocalizedAlias> findByFood_IdOrderByLocaleCodeAscAliasAsc(UUID foodId);
}
