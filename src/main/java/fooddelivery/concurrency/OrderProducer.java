package fooddelivery.concurrency;

import java.util.UUID;

import fooddelivery.observer.EventType;
import fooddelivery.observer.OrderEvent;
import fooddelivery.observer.Subject;

public class OrderProducer implements Runnable {
    private final Subject<OrderEvent> platform;
    private final int eventCount;
    private final String restaurantId;

    public OrderProducer(Subject<OrderEvent> platform, int eventCount, String restaurantId) {
        this.platform = platform;
        this.eventCount = eventCount;
        this.restaurantId = restaurantId;
    }

    @Override
    public void run() {
        for (int i = 0; i < eventCount; i++) {
            String orderId = UUID.randomUUID().toString();
            platform.notifyObservers(new OrderEvent(EventType.ORDER_PLACED, orderId, restaurantId, 15.0));
            platform.notifyObservers(new OrderEvent(EventType.ORDER_READY, orderId, restaurantId, 15.0));
            platform.notifyObservers(new OrderEvent(EventType.ORDER_DELIVERED, orderId, restaurantId, 15.0));
        }
    }
}