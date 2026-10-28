package fooddelivery.visitor;

public interface Element {
    <R> R accept(Visitor<R> visitor);
}