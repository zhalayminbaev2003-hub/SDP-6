package fooddelivery.visitor;

public record Sauce(String name, double price, boolean isSpicy) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}