package com.my.utils.world.com;

import com.my.utils.world.Component;
import com.my.utils.world.Entity;

public class Collision implements Component {
    public final int callbackFlag;
    public final int callbackFilter;
    private final Handler handler;

    public Collision(int callbackFlag) {
        this(callbackFlag, 0, null);
    }

    public Collision(int callbackFlag, int callbackFilter, Handler handler) {
        this.callbackFlag = callbackFlag;
        this.callbackFilter = callbackFilter;
        this.handler = handler;
    }

    public void handle(Entity self, Entity target) {
        if (handler != null) {
            handler.handle(self, target);
        }
    }

    public interface Handler {
        void handle(Entity self, Entity target);
    }
}
