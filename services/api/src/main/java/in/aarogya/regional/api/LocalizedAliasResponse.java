package in.aarogya.regional.api;

import in.aarogya.regional.domain.FoodLocalizedAlias;

public record LocalizedAliasResponse(
    String locale,
    String alias
) {

    public static LocalizedAliasResponse from(FoodLocalizedAlias value) {
        return new LocalizedAliasResponse(
            value.getLocaleCode(),
            value.getAlias()
        );
    }
}
