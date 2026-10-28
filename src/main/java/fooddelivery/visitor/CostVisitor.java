package fooddelivery.visitor;

public class CostVisitor implements Visitor<Double> {
    @Override public Double visit(City city) { return city.restaurants().stream().mapToDouble(r -> r.accept(this)).sum(); }
    @Override public Double visit(Restaurant restaurant) { return restaurant.menu().stream().mapToDouble(e -> e.accept(this)).sum(); }
    @Override public Double visit(Dish dish) { return dish.price(); }
    @Override public Double visit(Drink drink) { return drink.price(); }
    @Override public Double visit(Combo combo) { return combo.price(); }
    @Override public Double visit(Sauce sauce) { return sauce.price(); }
}