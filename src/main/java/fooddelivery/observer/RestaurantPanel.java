package fooddelivery.observer;

import fooddelivery.visitor.City;
import fooddelivery.visitor.CostVisitor;
import fooddelivery.visitor.Restaurant;

public class RestaurantPanel extends BaseSubject<OrderEvent> implements Observer<OrderEvent> {
    private final City city;

    public RestaurantPanel(City city) {
        this.city = city;
    }

    @Override
    public void update(OrderEvent event) {
        if (event.type() == EventType.ORDER_DELIVERED) {
            Restaurant target = city.restaurants().stream()
                    .filter(r -> r.id().equals(event.restaurantId()))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                double menuCost = target.accept(new CostVisitor());
                notifyObservers(new OrderEvent(EventType.PROFIT_UPDATED, event.orderId(), target.id(), menuCost));
            }
        }
    }
}