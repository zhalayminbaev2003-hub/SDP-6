package fooddelivery.visitor;

public record Drink(String name, double price, int volumeMl) implements Element {
    @Override 
    public <R> R accept(Visitor<R> visitor) { 
        return visitor.visit(this); 
    }
}