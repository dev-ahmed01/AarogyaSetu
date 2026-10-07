package in.aarogya.plans.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import in.aarogya.plans.domain.DietPlan;
import in.aarogya.plans.domain.DietPlanItem;

public record DietPlanResponse(
    UUID id,
    LocalDate planDate,
    String status,
    String generationMode,
    String engineStatus,
    String sourceRuleCode,
    Integer sourceRuleVersion,
    String regionalContextCode,
    String regionalContextLabel,
    Instant createdAt,
    List<DietPlanItemResponse> items,
    List<String> notices
) {
    public static DietPlanResponse from(DietPlan plan, List<String> notices) {
        return new DietPlanResponse(
            plan.getId(), plan.getPlanDate(), plan.getStatus(),
            plan.getGenerationMode(), plan.getEngineStatus(),
            plan.getSourceRuleCode(), plan.getSourceRuleVersion(),
            plan.getRegionalContextCode(), plan.getRegionalContextLabel(),
            plan.getCreatedAt(),
            plan.getItems().stream()
                .sorted(Comparator.comparingInt(DietPlanItem::getDisplayOrder))
                .map(DietPlanItemResponse::from)
                .toList(),
            notices
        );
    }
}
