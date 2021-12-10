package com.my.utils.world.sys;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.com.Motion;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;

import java.util.Map;

public class MotionSystem extends BaseSystem implements EntityListener {

    // ----- Check ----- //
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, RigidBody.class, Motion.class);
    }

    // ----- Custom ----- //
    public void update() {
        for (Entity entity : getEntities()) {
            Motion motion = entity.getComponent(Motion.class);

            // Get Motion Config
            Map<String, Object> config = motion.getConfig();

            // Update
            motion.getHandler().update(config, entity.getComponent(RigidBody.class).body, entity.getComponent(Position.class));
        }
    }

    @Override
    public void afterAdded(Entity entity) {
        Motion motion = entity.getComponent(Motion.class);

        String type = motion.getType();
        MotionHandler handler = world.getAssetsManager().getAsset(type, MotionHandler.class);
        if (handler == null) throw new RuntimeException("No such Motion Handler for this Type: " + type);

        motion.setHandler(handler);
    }

    @Override
    public void afterRemoved(Entity entity) {}

    public interface MotionHandler {
        void update(Map<String, Object> config, btRigidBody body, Position position);
    }

}
