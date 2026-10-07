package in.aarogya.regional.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.regional.domain.RegionalFoodAlternative;

public interface RegionalFoodAlternativeRepository
    extends JpaRepository<RegionalFoodAlternative, UUID> {

    List<RegionalFoodAlternative>
        findBySourceFood_SlugAndRegionCodeInAndActiveTrueOrderByPriorityAsc(
            String sourceFoodSlug,
            Collection<String> regionCodes
        );
}
