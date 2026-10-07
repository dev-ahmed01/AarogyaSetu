package in.aarogya.nudges.api;

import java.time.LocalDate;

public record NudgeEvaluationResponse(
    LocalDate evaluationDate,
    int signalsEvaluated,
    int activeAfterEvaluation,
    int resolvedDuringEvaluation
) {
}
