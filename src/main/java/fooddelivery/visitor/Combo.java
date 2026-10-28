package fooddelivery.visitor;

import java.util.List;

public record Combo(String name, double price, List<Element> items) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}