package com.my.utils.world;

public interface System extends Loadable {

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
