package in.aarogya.nudges.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nudges.domain.NudgeRule;

public interface NudgeRuleRepository extends JpaRepository<NudgeRule, UUID> {

    List<NudgeRule> findByActiveTrueOrderByRuleCodeAscRuleVersionDesc();
}
