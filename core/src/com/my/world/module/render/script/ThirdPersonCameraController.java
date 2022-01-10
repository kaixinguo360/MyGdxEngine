package com.my.world.module.render.script;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

public class ThirdPersonCameraController implements ScriptSystem.OnStart {

    @Config public float yaw = 0;
    @Config public float pitch = 0;
    @Config public float distance = 20;

    protected Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        this.position = entity.getComponent(Position.class);
        updateTransform();
    }

    public void updateTransform() {
        Matrix4 localTransform = position.getLocalTransform();
        localTransform.idt().rotate(Vector3.Y, yaw).rotate(Vector3.X, pitch).translate(0, 0, distance);
    }
}
