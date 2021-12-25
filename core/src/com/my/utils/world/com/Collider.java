package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Collider implements Component, Loadable.OnInit, Disposable {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config(type = Config.Type.Asset)
    public btCollisionShape shape;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    public btCollisionObject collisionObject;

    public Collider(btCollisionShape shape) {
        this.shape = shape;
        init();
    }

    @Override
    public void init() {
        this.collisionObject = new btCollisionObject();
        this.collisionObject.setCollisionShape(shape);
    }

    @Override
    public void dispose() {
        if (collisionObject != null) collisionObject.dispose();
    }
}
