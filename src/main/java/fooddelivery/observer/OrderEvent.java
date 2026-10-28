package fooddelivery.observer;

public record OrderEvent(EventType type, String orderId, String restaurantId, double amount) {}