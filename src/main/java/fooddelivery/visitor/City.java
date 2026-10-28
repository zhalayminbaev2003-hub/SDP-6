package fooddelivery.visitor;

import java.util.List;

public record City(String id, String name, List<Restaurant> restaurants) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}