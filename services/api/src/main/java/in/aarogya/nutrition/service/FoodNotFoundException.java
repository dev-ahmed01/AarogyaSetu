package in.aarogya.nutrition.service;

public class FoodNotFoundException extends RuntimeException {

    public FoodNotFoundException(String slug) {
        super("Food record not found: " + slug);
    }
}
