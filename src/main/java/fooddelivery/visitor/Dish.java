package fooddelivery.visitor;

import java.util.List;

public record Dish(String name, double price, int calories, List<String> allergens) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}