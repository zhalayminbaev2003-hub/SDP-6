package fooddelivery;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fooddelivery.concurrency.OrderProducer;
import fooddelivery.observer.AnalyticsDashboard;
import fooddelivery.observer.BaseSubject;
import fooddelivery.observer.CourierApp;
import fooddelivery.observer.CustomerApp;
import fooddelivery.observer.EtaCalculator;
import fooddelivery.observer.EventType;
import fooddelivery.observer.OrderEvent;
import fooddelivery.observer.RestaurantPanel;
import fooddelivery.visitor.AllergenVisitor;
import fooddelivery.visitor.City;
import fooddelivery.visitor.Combo;
import fooddelivery.visitor.CostVisitor;
import fooddelivery.visitor.Dish;
import fooddelivery.visitor.Drink;
import fooddelivery.visitor.Element;
import fooddelivery.visitor.HighCalorieFilterVisitor;
import fooddelivery.visitor.JsonExportVisitor;
import fooddelivery.visitor.Restaurant;
import fooddelivery.visitor.Sauce;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================");
        System.out.println("   FOOD DELIVERY SYSTEM - DEFENSE DEMO    ");
        System.out.println("==========================================\n");

        // --- BLOCK 1: Data Initialization ---
        Restaurant r1 = new Restaurant("R1", "BurgerKing", List.of(
                new Dish("Whopper", 12.0, 800, List.of("Gluten", "Dairy", "Meat")),
                new Drink("Cola", 2.0, 300),
                new Sauce("Ketchup", 0.5, false)
        ));
        Restaurant r2 = new Restaurant("R2", "Papa Johns", List.of(
                new Dish("Pepperoni", 15.0, 1200, List.of("Gluten", "Cheese", "Meat")),
                new Combo("Lunch Box", 18.0, List.of(
                        new Dish("Pasta", 12.0, 600, List.of("Gluten")),
                        new Drink("Juice", 3.0, 200)
                ))
        ));
        City city = new City("C1", "Astana", List.of(r1, r2));

        // --- BLOCK 2: VISITOR Pattern Demonstration ---
        System.out.println("--- 1. VISITOR PATTERN ---");
        
        CostVisitor costVisitor = new CostVisitor();
        System.out.println("[CostVisitor] Total cost of all menus in the city: $" + city.accept(costVisitor));

        AllergenVisitor allergenVisitor = new AllergenVisitor();
        @SuppressWarnings("unchecked")
        Set<String> allergens = (Set<String>) city.accept(allergenVisitor);
        System.out.println("[AllergenVisitor] Unique allergens found: " + allergens);

        HighCalorieFilterVisitor filterVisitor = new HighCalorieFilterVisitor(700);
        @SuppressWarnings("unchecked")
        List<Element> highCalorieItems = (List<Element>) city.accept(filterVisitor);
        System.out.println("[HighCalorieFilterVisitor] Number of items with calories > 700: " + highCalorieItems.size());

        JsonExportVisitor jsonVisitor = new JsonExportVisitor();
        System.out.println("[JsonExportVisitor] Exporting city data to JSON:");
        System.out.println(city.accept(jsonVisitor));


        // --- BLOCK 3: OBSERVER Pattern Demonstration ---
        System.out.println("\n--- 2. OBSERVER PATTERN ---");
        BaseSubject<OrderEvent> platform = new BaseSubject<>();
        
        // Create subscribers (Observers)
        RestaurantPanel panel = new RestaurantPanel(city);
        CustomerApp customer = new CustomerApp();
        CourierApp courier = new CourierApp();
        AnalyticsDashboard analytics = new AnalyticsDashboard();
        EtaCalculator eta = new EtaCalculator();

        // Distribute subscriptions by event type
        platform.subscribe(EventType.ORDER_PLACED, panel);
        platform.subscribe(EventType.ORDER_PLACED, analytics);
        platform.subscribe(EventType.ORDER_PLACED, eta);

        platform.subscribe(EventType.ORDER_READY, courier);
        platform.subscribe(EventType.ORDER_READY, customer);

        platform.subscribe(EventType.ORDER_DELIVERED, panel);
        platform.subscribe(EventType.ORDER_DELIVERED, customer);
        platform.subscribe(EventType.ORDER_DELIVERED, analytics);

        System.out.println(">>> Lifecycle of Order #ORD-001:");
        
        System.out.println("\n[Event] CUSTOMER PLACED ORDER (ORDER_PLACED)");
        platform.notifyObservers(new OrderEvent(EventType.ORDER_PLACED, "ORD-001", "R1", 14.5));

        System.out.println("\n[Event] RESTAURANT PREPARED ORDER (ORDER_READY)");
        platform.notifyObservers(new OrderEvent(EventType.ORDER_READY, "ORD-001", "R1", 14.5));

        System.out.println("\n[Event] COURIER DELIVERED ORDER (ORDER_DELIVERED)");
        platform.notifyObservers(new OrderEvent(EventType.ORDER_DELIVERED, "ORD-001", "R1", 14.5));


        // --- BLOCK 4: CONCURRENCY Demonstration ---
        System.out.println("\n--- 3. CONCURRENCY ---");
        System.out.println("Starting 3 independent order processing threads...");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Launch order generation threads (each will create 2 orders)
        for (int i = 1; i <= 3; i++) {
            String restaurantId = (i % 2 == 0) ? "R2" : "R1";
            executor.submit(new OrderProducer(platform, 2, restaurantId));
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n==========================================");
        System.out.println("   DEMONSTRATION SUCCESSFULLY COMPLETED   ");
        System.out.println("==========================================");
    }
}