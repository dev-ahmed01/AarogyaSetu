package in.aarogya.regional.api;

import java.util.List;

public record RegionalContextResponse(
    String profileValue,
    String stateCode,
    String stateLabel,
    String macroRegionCode,
    String macroRegionLabel,
    boolean supported,
    List<String> matchedRegionCodes,
    String basis,
    String disclaimer
) {
}
