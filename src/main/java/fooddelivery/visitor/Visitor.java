package fooddelivery.visitor;

public interface Visitor<R> {
    R visit(City city);
    R visit(Restaurant restaurant);
    R visit(Dish dish);
    R visit(Drink drink);
    R visit(Combo combo);
    R visit(Sauce sauce);
}