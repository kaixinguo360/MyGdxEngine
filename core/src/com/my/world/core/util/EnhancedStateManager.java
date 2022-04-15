package com.my.world.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedStateManager<T extends Enum<T>, E extends Enum<E>> extends StateManager<T> {

    public final Map<E, List<ActionInner>> actions = new HashMap<>();

    public EnhancedStateManager(T currentState) {
        super(currentState);
    }

    public void addAction(E action, T fromState, T toState) {
        List<ActionInner> list = actions.computeIfAbsent(action, k -> new ArrayList<>());

        ActionInner actionInner = new ActionInner();
        actionInner.fromState = fromState;
        actionInner.toState = toState;

        list.add(actionInner);
    }
    public void addAction(E action, T fromState, float waitTime, T toState) {
        List<ActionInner> list = actions.computeIfAbsent(action, k -> new ArrayList<>());

        ActionInner actionInner = new ActionInner();
        actionInner.fromState = fromState;
        actionInner.toState = toState;
        actionInner.waitTime = waitTime;

        list.add(actionInner);
    }

    public void doAction(E action) {
        List<ActionInner> list = actions.computeIfAbsent(action, k -> new ArrayList<>());
        for (ActionInner actionInner : list) {
            if (actionInner.waitTime == null) {
                switchState(actionInner.fromState, actionInner.toState);
            } else {
                switchState(actionInner.fromState, actionInner.waitTime, actionInner.toState);
            }
        }
    }

    public class ActionInner {
        public T fromState;
        public T toState;
        public Float waitTime;
    }
}
