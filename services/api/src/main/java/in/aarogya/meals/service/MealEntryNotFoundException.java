package in.aarogya.meals.service;

public class MealEntryNotFoundException extends RuntimeException {

    public MealEntryNotFoundException() {
        super("Meal entry was not found.");
    }
}
