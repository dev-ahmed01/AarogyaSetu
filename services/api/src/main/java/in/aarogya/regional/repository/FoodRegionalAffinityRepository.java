package in.aarogya.regional.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.regional.domain.FoodRegionalAffinity;

public interface FoodRegionalAffinityRepository
    extends JpaRepository<FoodRegionalAffinity, UUID> {

    List<FoodRegionalAffinity> findByFood_IdInAndRegionCodeIn(
        Collection<UUID> foodIds,
        Collection<String> regionCodes
    );

    List<FoodRegionalAffinity> findByRegionCodeInOrderByAffinityScoreDesc(
        Collection<String> regionCodes
    );
}
