package fooddelivery.observer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseSubject<T> implements Subject<T> {
    private final Map<EventType, List<Observer<T>>> registry = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<T> observer) {
        registry.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<T> observer) {
        registry.getOrDefault(type, new CopyOnWriteArrayList<>()).remove(observer);
    }

    @Override
    public void notifyObservers(T event) {
        OrderEvent e = (OrderEvent) event;
        registry.getOrDefault(e.type(), new CopyOnWriteArrayList<>()).forEach(obs -> obs.update(event));
    }
}