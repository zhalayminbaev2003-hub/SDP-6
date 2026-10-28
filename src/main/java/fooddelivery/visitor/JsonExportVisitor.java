package fooddelivery.visitor;

import java.util.stream.Collectors;

public class JsonExportVisitor implements Visitor<String> {
    @Override public String visit(City city) { return "{ \"city\": \"" + city.name() + "\", \"restaurants\": [" + city.restaurants().stream().map(r -> r.accept(this)).collect(Collectors.joining(",")) + "] }"; }
    @Override public String visit(Restaurant restaurant) { return "{ \"restaurant\": \"" + restaurant.name() + "\", \"menu\": [" + restaurant.menu().stream().map(e -> e.accept(this)).collect(Collectors.joining(",")) + "] }"; }
    @Override public String visit(Dish dish) { return "{ \"dish\": \"" + dish.name() + "\", \"price\": " + dish.price() + " }"; }
    @Override public String visit(Drink drink) { return "{ \"drink\": \"" + drink.name() + "\", \"price\": " + drink.price() + " }"; }
    @Override public String visit(Combo combo) { return "{ \"combo\": \"" + combo.name() + "\", \"price\": " + combo.price() + " }"; }
    @Override public String visit(Sauce sauce) { return "{ \"sauce\": \"" + sauce.name() + "\", \"price\": " + sauce.price() + " }"; }
}