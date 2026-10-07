package in.aarogya.recommendations.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.recommendations.domain.RecommendationRule;

public interface RecommendationRuleRepository
    extends JpaRepository<RecommendationRule, UUID> {

    List<RecommendationRule> findByActiveTrueOrderByRuleCodeAscRuleVersionDesc();
}
