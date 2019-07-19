package com.my.utils.world;

public interface Module {
    void add(Component component, String instanceName);
    String get(Component component);
    void remove(Component component);
    boolean handle(Component component);
}
