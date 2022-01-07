package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Disposable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RigidBody implements Component, Disposable {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    @Config
    public boolean isTrigger;

    public btRigidBody body;

    protected RigidBody(boolean isTrigger) {
        this.isTrigger = isTrigger;
    }

    public RigidBody(btRigidBody body, boolean isTrigger) {
        this(isTrigger);
        this.body = body;
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
