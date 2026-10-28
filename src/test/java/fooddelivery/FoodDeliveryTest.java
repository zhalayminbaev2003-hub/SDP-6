package fooddelivery;

import fooddelivery.concurrency.OrderProducer;
import fooddelivery.observer.*;
import fooddelivery.visitor.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FoodDeliveryTest {
    private City city;
    private BaseSubject<OrderEvent> platform;
    private RestaurantPanel panel;

    @BeforeEach
    @SuppressWarnings("unused")
    public void setUp() {
        Restaurant r1 = new Restaurant("R1", "Burger Place", List.of(
                new Dish("Burger", 10.0, 800, List.of("Gluten", "Dairy")),
                new Drink("Cola", 2.0, 500),
                new Sauce("Ketchup", 0.5, false)
        ));
        Restaurant r2 = new Restaurant("R2", "Pizza Place", List.of(
                new Dish("Pizza", 15.0, 1200, List.of("Gluten", "Cheese")),
                new Combo("Lunch", 18.0, List.of(new Dish("Pasta", 12.0, 600, List.of("Gluten"))))
        ));
        city = new City("C1", "Astana", List.of(r1, r2));
        platform = new BaseSubject<>();
        panel = new RestaurantPanel(city);
    }

    @Test
    void testNumericVisitor() {
        CostVisitor costVisitor = new CostVisitor();
        double r1Cost = ((Number) city.restaurants().get(0).accept(costVisitor)).doubleValue();
        double r2Cost = ((Number) city.restaurants().get(1).accept(costVisitor)).doubleValue();
        double cityCost = ((Number) city.accept(costVisitor)).doubleValue();

        assertEquals(12.5, r1Cost, 0.001);
        assertEquals(33.0, r2Cost, 0.001);
        assertEquals(45.5, cityCost, 0.001);
    }

    @Test
    void testAccumulatorVisitor() {
        AllergenVisitor allergenVisitor = new AllergenVisitor();
        @SuppressWarnings("unchecked")
        Set<String> allergens = (Set<String>) city.accept(allergenVisitor);
        assertTrue(allergens.containsAll(List.of("Gluten", "Dairy", "Cheese")));
    }

    @Test
    void testFilterVisitor() {
        HighCalorieFilterVisitor filter = new HighCalorieFilterVisitor(700);
        @SuppressWarnings("unchecked")
        List<Element> highCal = (List<Element>) city.accept(filter);
        assertEquals(2, highCal.size());
    }

    @Test
    void testJsonVisitor() {
        JsonExportVisitor jsonVisitor = new JsonExportVisitor();
        String json = (String) city.accept(jsonVisitor);
        assertTrue(json.contains("Astana") || json.contains("\"city\": \"Astana\""));
        assertTrue(json.contains("Pizza") || json.contains("\"dish\": \"Pizza\""));
    }

    @Test
    void testObserverSubscription() {
        AtomicInteger count = new AtomicInteger(0);
        Observer<OrderEvent> obs = e -> count.incrementAndGet();
        platform.subscribe(EventType.ORDER_PLACED, obs);
        platform.notifyObservers(new OrderEvent(EventType.ORDER_PLACED, "1", "R1", 10));
        assertEquals(1, count.get());
    }

    @Test
    void testUnsubscribeDuringNotification() {
        AtomicInteger count = new AtomicInteger(0);
        Observer<OrderEvent> obs = new Observer<OrderEvent>() {
            @Override
            public void update(OrderEvent event) {
                count.incrementAndGet();
                platform.unsubscribe(EventType.ORDER_PLACED, this);
            }
        };
        platform.subscribe(EventType.ORDER_PLACED, obs);
        platform.notifyObservers(new OrderEvent(EventType.ORDER_PLACED, "1", "R1", 10));
        platform.notifyObservers(new OrderEvent(EventType.ORDER_PLACED, "2", "R1", 10));
        assertEquals(1, count.get());
    }

    @Test
    void testIntegrationObserverTriggersVisitor() {
        AtomicInteger profitEvents = new AtomicInteger(0);
        panel.subscribe(EventType.PROFIT_UPDATED, e -> profitEvents.incrementAndGet());
        platform.subscribe(EventType.ORDER_DELIVERED, panel);

        platform.notifyObservers(new OrderEvent(EventType.ORDER_DELIVERED, "1", "R1", 10));
        assertEquals(1, profitEvents.get());
    }

    @Test
    void testStressConcurrency() throws InterruptedException {
        AtomicInteger totalDelivered = new AtomicInteger(0);
        platform.subscribe(EventType.ORDER_DELIVERED, e -> totalDelivered.incrementAndGet());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            executor.submit(new OrderProducer(platform, 1000, "R1"));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(4000, totalDelivered.get());
    }

    @Test
    void testVisitorDoubleDispatchDish() {
        double cost = ((Number) new Dish("X", 10.0, 1, List.of()).accept(new CostVisitor())).doubleValue();
        assertEquals(10.0, cost, 0.001);
    }

    @Test
    void testVisitorDoubleDispatchDrink() {
        double cost = ((Number) new Drink("Y", 5.0, 1).accept(new CostVisitor())).doubleValue();
        assertEquals(5.0, cost, 0.001);
    }

    @Test
    void testVisitorDoubleDispatchSauce() {
        double cost = ((Number) new Sauce("Z", 1.0, true).accept(new CostVisitor())).doubleValue();
        assertEquals(1.0, cost, 0.001);
    }

    @Test
    void testVisitorDoubleDispatchCombo() {
        double cost = ((Number) new Combo("C", 15.0, List.of()).accept(new CostVisitor())).doubleValue();
        assertEquals(15.0, cost, 0.001);
    }

    @Test
    void testIntegrationWrongRestaurant() {
        AtomicInteger profitEvents = new AtomicInteger(0);
        panel.subscribe(EventType.PROFIT_UPDATED, e -> profitEvents.incrementAndGet());
        platform.subscribe(EventType.ORDER_DELIVERED, panel);
        platform.notifyObservers(new OrderEvent(EventType.ORDER_DELIVERED, "1", "INVALID", 10));
        assertEquals(0, profitEvents.get());
    }

    @Test
    void testMultipleListeners() {
        AtomicInteger c1 = new AtomicInteger(0);
        AtomicInteger c2 = new AtomicInteger(0);
        platform.subscribe(EventType.ORDER_READY, e -> c1.incrementAndGet());
        platform.subscribe(EventType.ORDER_READY, e -> c2.incrementAndGet());
        platform.notifyObservers(new OrderEvent(EventType.ORDER_READY, "1", "R1", 10));
        assertEquals(1, c1.get());
        assertEquals(1, c2.get());
    }

    @Test
    void testIgnoreUnsubscribedEvents() {
        AtomicInteger count = new AtomicInteger(0);
        platform.subscribe(EventType.ORDER_PLACED, e -> count.incrementAndGet());
        platform.notifyObservers(new OrderEvent(EventType.ORDER_DELIVERED, "1", "R1", 10));
        assertEquals(0, count.get());
    }
}