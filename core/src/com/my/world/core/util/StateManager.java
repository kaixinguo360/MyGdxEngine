package com.my.world.core.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateManager<T extends Enum<T>> {

    @Getter
    protected T currentState;

    @Getter
    protected long lastSwitchTime;

    public final Map<T, Map<T, List<Runnable>>> switchFunctions = new HashMap<>();
    public final Map<T, List<Runnable>> leaveFunctions = new HashMap<>();
    public final Map<T, List<Runnable>> enterFunctions = new HashMap<>();

    public StateManager(T currentState) {
        this.currentState = currentState;
        this.lastSwitchTime = System.currentTimeMillis();
    }

    public void whenSwitch(T fromState, T toState, Runnable fun) {
        Map<T, List<Runnable>> map = switchFunctions.computeIfAbsent(fromState, k -> new HashMap<>());
        List<Runnable> list = map.computeIfAbsent(toState, k -> new ArrayList<>());
        list.add(fun);
    }
    public void whenLeave(T fromState, Runnable fun) {
        List<Runnable> list = leaveFunctions.computeIfAbsent(fromState, k -> new ArrayList<>());
        list.add(fun);
    }
    public void whenEnter(T toState, Runnable fun) {
        List<Runnable> list = enterFunctions.computeIfAbsent(toState, k -> new ArrayList<>());
        list.add(fun);
    }

    public void invoke(T state, Runnable fun) {
        if (currentState == state) {
            fun.run();
        }
    }
    public void invoke(T state, long waitTime, Runnable fun) {
        if (currentState == state && (System.currentTimeMillis() - lastSwitchTime) > waitTime) {
            fun.run();
        }
    }

    public void switchState(T fromState, T toState) {
        if (currentState != fromState) return;
        switchState(toState);
    }
    public void switchState(T fromState, long waitTime, T toState) {
        if (currentState != fromState) return;
        if ((System.currentTimeMillis() - lastSwitchTime) <= waitTime) return;
        switchState(toState);
    }
    public void switchState(T toState) {
        invokeLeaveFunction(currentState);
        currentState = toState;
        lastSwitchTime = System.currentTimeMillis();
        invokeSwitchFunction(currentState, toState);
        invokeEnterFunction(toState);
    }

    protected void invokeSwitchFunction(T fromState, T toState) {
        Map<T, List<Runnable>> map = switchFunctions.get(fromState);
        if (map == null) return;

        List<Runnable> list = map.get(toState);
        if (list == null) return;

        for (Runnable fun : list) {
            fun.run();
        }
    }
    protected void invokeLeaveFunction(T fromState) {
        List<Runnable> list = leaveFunctions.get(fromState);
        if (list == null) return;

        for (Runnable fun : list) {
            fun.run();
        }
    }
    protected void invokeEnterFunction(T toState) {
        List<Runnable> list = enterFunctions.get(toState);
        if (list == null) return;

        for (Runnable fun : list) {
            fun.run();
        }
    }
}
