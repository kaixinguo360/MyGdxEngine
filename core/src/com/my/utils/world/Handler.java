package com.my.utils.world;

public interface Handler {
    void add(Module module, String instanceName);
    void remove(Module module);
    boolean handle(Module module);
}
