package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Config;
import com.my.world.module.common.BaseActivatableComponent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RigidBody extends BaseActivatableComponent {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    @Config
    public Integer collisionFlags;

    @Config
    public boolean isStatic;

    @Config
    public boolean isKinematic;

    @Config
    public boolean isTrigger;

    @Config
    public boolean autoConvertToWorldTransform = false;

    @Config
    public boolean autoConvertToLocalTransform = false;

    public btRigidBody body;
    public PhysicsSystem system;

    protected RigidBody(boolean isTrigger) {
        this.isTrigger = isTrigger;
    }

    public RigidBody(btRigidBody body, boolean isTrigger) {
        this(isTrigger);
        this.body = body;
    }

    public void reset() {
        if (system == null) throw new RuntimeException("This component not attached to a PhysicsSystem: " + this);
        if (entity == null) throw new RuntimeException("This component not attached to an entity: " + this);
        if (!system.canHandle(this.entity)) throw new RuntimeException("This component is not active: " + this);
        PhysicsSystem system = this.system;
        system.afterEntityRemoved(this.entity);
        system.afterEntityAdded(this.entity);
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
