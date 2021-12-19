package com.my.utils.world.com;

import com.my.utils.world.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Script implements Component, StandaloneResource {

    @Config
    public boolean disabled;

    public boolean running = false;

    public interface OnStart extends Component {
        void start(World world, Entity entity);
    }

    public interface OnUpdate extends Component {
        void update(World world, Entity entity);
    }

    public interface OnKeyDown extends Component {
        void keyDown(World world, Entity entity, int keycode);
    }

}
