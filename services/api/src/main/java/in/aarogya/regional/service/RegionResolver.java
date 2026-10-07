package in.aarogya.regional.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import in.aarogya.regional.api.RegionalContextResponse;

@Component
public class RegionResolver {

    private static final Map<String, String> MACRO_LABELS = Map.of(
        "NORTH_INDIA", "North India",
        "SOUTH_INDIA", "South India",
        "EAST_INDIA", "East India",
        "WEST_INDIA", "West India",
        "CENTRAL_INDIA", "Central India",
        "NORTHEAST_INDIA", "Northeast India"
    );

    private static final Map<String, String> STATE_TO_MACRO = stateMap();

    public RegionalContextResponse resolve(String profileValue) {
        if (profileValue == null || profileValue.isBlank()) {
            return new RegionalContextResponse(
                null,
                null,
                null,
                null,
                null,
                false,
                List.of("ALL_INDIA"),
                "NO_PROFILE_REGION",
                "Regional ranking is inactive because no state or region is stored in the profile."
            );
        }

        var normalized = normalize(profileValue);

        if (MACRO_LABELS.containsKey(normalized)) {
            return new RegionalContextResponse(
                profileValue,
                null,
                null,
                normalized,
                MACRO_LABELS.get(normalized),
                true,
                List.of(normalized, "ALL_INDIA"),
                "SELF_REPORTED_PROFILE_REGION",
                disclaimer()
            );
        }

        var macro = STATE_TO_MACRO.get(normalized);
        var stateCode = "STATE_" + normalized;

        if (macro == null) {
            return new RegionalContextResponse(
                profileValue,
                stateCode,
                profileValue.trim(),
                null,
                null,
                false,
                List.of("ALL_INDIA"),
                "UNMAPPED_PROFILE_REGION",
                "This profile region is stored, but Phase 11 has no broad regional grouping for it yet. Only all-India familiarity metadata is used."
            );
        }

        return new RegionalContextResponse(
            profileValue,
            stateCode,
            profileValue.trim(),
            macro,
            MACRO_LABELS.get(macro),
            true,
            List.of(stateCode, macro, "ALL_INDIA"),
            "SELF_REPORTED_PROFILE_REGION",
            disclaimer()
        );
    }

    private String disclaimer() {
        return "Regional fit is a broad cultural-familiarity heuristic from the self-reported profile state/region. It does not infer ethnicity, religion, exact location, or nutritional need.";
    }

    private String normalize(String value) {
        return value.trim()
            .toUpperCase(Locale.ROOT)
            .replace("&", "AND")
            .replaceAll("[^A-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private static Map<String, String> stateMap() {
        var values = new LinkedHashMap<String, String>();

        add(values, "SOUTH_INDIA",
            "ANDHRA_PRADESH", "KARNATAKA", "KERALA", "TAMIL_NADU",
            "TELANGANA", "PUDUCHERRY", "LAKSHADWEEP"
        );
        add(values, "NORTH_INDIA",
            "CHANDIGARH", "DELHI", "HARYANA", "HIMACHAL_PRADESH",
            "JAMMU_AND_KASHMIR", "LADAKH", "PUNJAB", "RAJASTHAN",
            "UTTAR_PRADESH", "UTTARAKHAND"
        );
        add(values, "WEST_INDIA",
            "DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU",
            "GOA", "GUJARAT", "MAHARASHTRA"
        );
        add(values, "CENTRAL_INDIA",
            "CHHATTISGARH", "MADHYA_PRADESH"
        );
        add(values, "EAST_INDIA",
            "BIHAR", "JHARKHAND", "ODISHA", "WEST_BENGAL"
        );
        add(values, "NORTHEAST_INDIA",
            "ARUNACHAL_PRADESH", "ASSAM", "MANIPUR", "MEGHALAYA",
            "MIZORAM", "NAGALAND", "SIKKIM", "TRIPURA"
        );

        return Map.copyOf(values);
    }

    private static void add(
        Map<String, String> map,
        String macro,
        String... states
    ) {
        for (var state : states) {
            map.put(state, macro);
        }
    }
}
