package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Disposable;
import com.my.world.core.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RigidBody implements Component, Loadable.OnInit, Disposable {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config(type = Config.Type.Asset)
    public RigidBodyConfig rigidBodyConfig;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    public btRigidBody body;

    public RigidBody(RigidBodyConfig rigidBodyConfig) {
        this.rigidBodyConfig = rigidBodyConfig;
        init();
    }

    @Override
    public void init() {
        this.body = new btRigidBody(rigidBodyConfig.constructionInfo);
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }
}
