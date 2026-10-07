package in.aarogya.progress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.progress.domain.WellnessGoalTemplate;

public interface WellnessGoalTemplateRepository
    extends JpaRepository<WellnessGoalTemplate, String> {

    List<WellnessGoalTemplate> findByActiveTrueOrderByGoalCodeAsc();
}
