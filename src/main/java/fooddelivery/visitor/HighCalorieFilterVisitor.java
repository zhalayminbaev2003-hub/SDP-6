package fooddelivery.visitor;

import java.util.List;

public class HighCalorieFilterVisitor implements Visitor<List<Element>> {
    private final int threshold;

    public HighCalorieFilterVisitor(int threshold) {
        this.threshold = threshold;
    }

    @Override public List<Element> visit(City city) { return city.restaurants().stream().flatMap(r -> r.accept(this).stream()).toList(); }
    @Override public List<Element> visit(Restaurant restaurant) { return restaurant.menu().stream().flatMap(e -> e.accept(this).stream()).toList(); }
    @Override public List<Element> visit(Dish dish) { return dish.calories() > threshold ? List.of(dish) : List.of(); }
    @Override public List<Element> visit(Drink drink) { return List.of(); }
    @Override public List<Element> visit(Combo combo) { return combo.items().stream().flatMap(e -> e.accept(this).stream()).toList(); }
    @Override public List<Element> visit(Sauce sauce) { return List.of(); }
}