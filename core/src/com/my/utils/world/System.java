package com.my.utils.world;

public interface System {

    interface AfterAdded {
        void afterAdded(World world);
    }

    interface AfterRemoved {
        void afterRemoved(World world);
    }

    interface OnStart {
        void start(World world);
    }

    interface OnUpdate {
        void update(float deltaTime);
    }

    interface OnKeyDown {
        void keyDown(int keycode);
    }
}
