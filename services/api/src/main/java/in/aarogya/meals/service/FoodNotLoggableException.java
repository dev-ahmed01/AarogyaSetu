package in.aarogya.meals.service;

public class FoodNotLoggableException extends RuntimeException {

    public FoodNotLoggableException(String foodName) {
        super(foodName + " does not yet have source-referenced nutrient data for meal logging.");
    }
}
