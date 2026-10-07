package in.aarogya.progress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.progress.domain.AchievementDefinition;

public interface AchievementDefinitionRepository
    extends JpaRepository<AchievementDefinition, String> {

    List<AchievementDefinition> findByActiveTrueOrderByDisplayOrderAsc();
}
