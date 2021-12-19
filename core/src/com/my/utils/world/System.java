package com.my.utils.world;

public interface System {

    interface AfterAdded extends System {
        void afterAdded(World world);
    }

    interface AfterRemoved extends System {
        void afterRemoved(World world);
    }

    interface OnStart extends System {
        void start(World world);
    }

    interface OnUpdate extends System {
        void update(float deltaTime);
    }

    interface OnKeyDown extends System {
        void keyDown(int keycode);
    }
}
