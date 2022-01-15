package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager implements Disposable {

    @Getter
    private final List<EventManager> children = new ArrayList<>();
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    public void addEventListener(String eventId, EventListener eventListener) {
        List<EventListener> list = listeners.computeIfAbsent(eventId, k -> new ArrayList<>());
        list.add(eventListener);
    }

    public void removeEventListener(String eventId, EventListener eventListener) {
        List<EventListener> list = listeners.get(eventId);
        if (list != null) {
            list.remove(eventListener);
        }
    }

    public void dispatchEvent(String eventId, Event event) {
        dispatchEvent(eventId, event, true);
    }

    private void dispatchEvent(String eventId, Event event, boolean dispose) {
        List<EventListener> list = listeners.get(eventId);
        if (list != null) {
            for (EventListener eventListener : list) {
                eventListener.onEvent(event);
            }
        }
        for (EventManager eventManager : children) {
            eventManager.dispatchEvent(eventId, event, false);
        }
        if (dispose) event.dispose();
    }

    @Override
    public void dispose() {
        children.clear();
        for (List<EventListener> list : listeners.values()) {
            list.clear();
        }
        listeners.clear();
    }
}
