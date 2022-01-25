package com.my.world.core;

public interface System extends Configurable {

    interface AfterAdded extends System {
        void afterAdded(Scene scene);
    }

    interface AfterRemoved extends System {
        void afterRemoved(Scene scene);
    }

    interface OnStart extends System {
        void start(Scene scene);
    }

    interface OnUpdate extends System {
        void update(float deltaTime);
    }
}
