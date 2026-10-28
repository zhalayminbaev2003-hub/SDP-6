package fooddelivery.visitor;

import java.util.List;

public record Restaurant(String id, String name, List<Element> menu) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}