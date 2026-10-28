package fooddelivery.visitor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AllergenVisitor implements Visitor<Set<String>> {
    @Override public Set<String> visit(City city) { return city.restaurants().stream().flatMap(r -> r.accept(this).stream()).collect(Collectors.toSet()); }
    @Override public Set<String> visit(Restaurant restaurant) { return restaurant.menu().stream().flatMap(e -> e.accept(this).stream()).collect(Collectors.toSet()); }
    @Override public Set<String> visit(Dish dish) { return new HashSet<>(dish.allergens()); }
    @Override public Set<String> visit(Drink drink) { return new HashSet<>(); }
    @Override public Set<String> visit(Combo combo) { return combo.items().stream().flatMap(e -> e.accept(this).stream()).collect(Collectors.toSet()); }
    @Override public Set<String> visit(Sauce sauce) { return new HashSet<>(); }
}