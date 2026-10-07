package in.aarogya.research.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.research.domain.ResearchMetricDefinition;
import in.aarogya.research.domain.ResearchMetricDefinitionId;

public interface ResearchMetricDefinitionRepository
    extends JpaRepository<ResearchMetricDefinition, ResearchMetricDefinitionId> {

    List<ResearchMetricDefinition> findAllByOrderByMetricCodeAscMetricVersionDesc();
}
