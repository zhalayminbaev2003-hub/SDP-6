<!-- README.md -->
# Assignment 6: Observer and Visitor Patterns (Variant 14: Food Delivery)

## Architecture Decisions
1. **Push vs Pull (Observer):** Implemented **Push**. Events (`OrderEvent`) carry their own data (`type`, `orderId`, `restaurantId`, `amount`). This prevents listeners from querying the Subject and tight-coupling to its state.
2. **Traversal Control (Visitor):** The **Visitor** dictates traversal (see `CostVisitor.visit(City)`), not the Elements. This keeps `Element` classes strictly as pure Data Objects without traversal logic.
3. **Integration Link:** `RestaurantPanel` acts as an Observer (listens to `ORDER_DELIVERED`) and a Subject (publishes `PROFIT_UPDATED`). Upon receiving an event, it runs `CostVisitor` on the specific `Restaurant` subtree.
4. **Concurrency:** Used `ConcurrentHashMap` and `CopyOnWriteArrayList` in the Subject registry. This allows lock-free safe iteration and permits listeners to unsubscribe themselves inside their own `update()` method.

## Trade-off Commits
* **Cost of new Visitor:** (Simulated Commit 1). Adding `JsonExportVisitor` touched 1 new file. Cost is very low. No Element classes were modified (OCP satisfied).
* **Cost of new Element:** (Simulated Commit 2). Adding `Sauce` required modifying `Element.java`, `Visitor.java`, and **all 4 concrete visitors**. Cost is high. Visitor pattern should be rejected if the element hierarchy changes frequently.
