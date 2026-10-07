package in.aarogya.regional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import in.aarogya.regional.service.RegionResolver;

class RegionResolverTests {

    private final RegionResolver resolver = new RegionResolver();

    @Test
    void karnatakaResolvesToStateSouthAndAllIndiaHierarchy() {
        var context = resolver.resolve("Karnataka");

        assertTrue(context.supported());
        assertEquals("STATE_KARNATAKA", context.stateCode());
        assertEquals("SOUTH_INDIA", context.macroRegionCode());
        assertEquals(
            java.util.List.of(
                "STATE_KARNATAKA",
                "SOUTH_INDIA",
                "ALL_INDIA"
            ),
            context.matchedRegionCodes()
        );
    }

    @Test
    void unknownRegionFallsBackWithoutInventingAMacroRegion() {
        var context = resolver.resolve("Custom Region");

        assertEquals(false, context.supported());
        assertEquals(
            java.util.List.of("ALL_INDIA"),
            context.matchedRegionCodes()
        );
    }
}
