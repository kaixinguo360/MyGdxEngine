package com.my.utils.world.com;

import com.my.utils.world.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Script implements Component, StandaloneResource {

    @Config
    public boolean disabled;

    public interface OnInit {
        void init(World world, Entity entity);
    }

    public interface OnUpdate {
        void update(World world, Entity entity);
    }

    public interface OnKeyDown {
        void keyDown(World world, Entity entity, int keycode);
    }
}
