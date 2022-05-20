package com.my.world.module.animation;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

public class DefaultAnimationController implements AnimationController {

    @Setter
    @Getter
    public String initState;
    public final List<State> states = new ArrayList<>();
    public final List<Transition> transitions = new ArrayList<>();
    protected final Map<String, State> stateCache = new HashMap<>();

    public State addState(String name, String playableName) {
        return addState(new State(name, playableName));
    }
    public State addState(State state) {
        String name = state.name;
        if (name == null) throw new RuntimeException("Name of state can not be null");
        try {
            getState(name);
            throw new RuntimeException("Duplicate State: " + name);
        } catch (Exception ignored) {}
        stateCache.put(name, state);
        states.add(state);
        return state;
    }
    public void removeState(String name) {
        if (name == null) throw new RuntimeException("Name of state can not be null");
        boolean isRemoved = states.removeIf(s -> Objects.equals(s.name, name));
        if (isRemoved) {
            stateCache.remove(name);
        } else {
            throw new RuntimeException("No Such State: " + name);
        }
    }
    public State getState(String name) {
        if (name == null) throw new RuntimeException("Name of state can not be null");
        State state = stateCache.get(name);
        if (state == null) {
            state = states.stream().filter(s -> Objects.equals(s.name, name)).findFirst().orElse(null);
            stateCache.put(name, state);
        }
        if (state == null) {
            throw new RuntimeException("No Such State: " + name);
        }
        return state;
    }

    public Transition addTransition(Transition transition) {
        transitions.add(transition);
        return transition;
    }
    public void removeTransition(Transition transition) {
        transitions.remove(transition);
    }

    @Override
    public AnimationController.Instance newInstance(Animation animation) {
        return new Instance(animation);
    }

    public static class State {

        public String name;
        public String playableName;

        public State(String name, String playableName) {
            this.name = name;
            this.playableName = playableName;
        }

        public void update(float currentTime, float weights, Instance instance) {
            weights = Math.max(Math.min(weights, 1), -1);
            Animation animation = instance.animation;
            Playable playable = animation.getPlayable(playableName);
            playable.update(currentTime, weights, animation);
        }
    }

    public static class Transition {

        public String nextState;

        public float start;
        public float end;
        public float offset;

        public boolean solo = false;
        public boolean mute = false;
        public boolean self = false;

        public Function<Instance, Boolean> canSwitch;
        public boolean canSwitch(Instance instance) {
            if (canSwitch != null) {
                return canSwitch.apply(instance);
            } else {
                return false;
            }
        }
    }

    public class Instance implements AnimationController.Instance {

        public Animation animation;

        public float currentTime;
        public float lastStateSwitchTime;
        public float lastTransitionSwitchTime;

        public State currentState;
        public Transition currentTransition;
        public State nextState;

        public Instance(Animation animation) {
            this.animation = animation;
            if (initState != null) {
                currentState = getState(initState);
            }
        }

        public void checkTransitionSwitch() {
            if (currentTransition != null && currentTransition.solo) return;
            for (Transition transition : transitions) {
                if (transition.mute) continue;
                if (currentTransition == transition) continue;
                if (!transition.self && currentState != null) {
                    if (Objects.equals(currentState.name, transition.nextState)) continue;
                }
                if (transition.canSwitch(this)) {
                    switchTransition(transition);
                    break;
                }
            }
        }

        public void updateCurrentState(float weights) {
            if (currentState == null) throw new RuntimeException("Illegal State: currentState=null");
            float timeSinceStateSwitch = currentTime - lastStateSwitchTime;
            if (currentTransition == null) {
                // No Transition
                currentState.update(timeSinceStateSwitch, weights, this);
            } else {
                // Update Transition: transition.start < time < transition.end
                float timeSinceTransitionSwitch = currentTime - lastTransitionSwitchTime;
                if (timeSinceTransitionSwitch >= currentTransition.start) {
                    float timeSinceTransitionStart = timeSinceTransitionSwitch - currentTransition.start;
                    float transitionLength = currentTransition.end - currentTransition.start;
                    float nextStatePercent = timeSinceTransitionStart / transitionLength;
                    nextStatePercent = Math.max(Math.min(nextStatePercent, 1), 0);
                    float currentStatePercent = 1 - nextStatePercent;
//                    System.out.println(currentStatePercent + "/" + nextStatePercent);
                    currentState.update(timeSinceStateSwitch, weights * currentStatePercent, this);
                    nextState.update(timeSinceTransitionStart + currentTransition.offset, weights * nextStatePercent, this);
                } else {
                    currentState.update(timeSinceStateSwitch, weights, this);
                }
                // Switch Transition: transition.end < time
                if (timeSinceTransitionSwitch >= currentTransition.end) {
                    switchState();
                }
            }
        }

        public void switchTransition(Transition transition) {
//            System.out.println("switchTransition: " + transition);
            if (currentTransition != null && currentTransition.solo) {
                throw new RuntimeException("Illegal State: currentTransition=" + currentTransition + ", nextState=" + nextState);
            }
            currentTransition = transition;
            nextState = getState(transition.nextState);
            lastTransitionSwitchTime = currentTime;
        }

        public void switchState() {
//            System.out.println("switchState: " + nextState);
            if (currentTransition == null || nextState == null) {
                throw new RuntimeException("Illegal State: currentTransition=" + currentTransition + ", nextState=" + nextState);
            }
            currentState = nextState;
            nextState = null;
            lastStateSwitchTime = lastTransitionSwitchTime + currentTransition.start - currentTransition.offset;
            currentTransition = null;
            lastTransitionSwitchTime = 0;
        }

        public void update(float currentTime, float weights) {
            this.currentTime = currentTime;
            checkTransitionSwitch();
            updateCurrentState(weights);
        }
    }
}
