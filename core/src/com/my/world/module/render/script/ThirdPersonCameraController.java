package com.my.world.module.render.script;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

public class ThirdPersonCameraController extends ActivatableComponent implements ScriptSystem.OnStart {

    @Config public float localYaw = 0;
    @Config public float localPitch = 0;

    @Config public float yaw = 0;
    @Config public float pitch = 0;

    @Config public Vector3 center = new Vector3(0, 0, 0);
    @Config public Vector3 translate = new Vector3(0, 0, 20);

    protected Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        this.position = entity.getComponent(Position.class);
        updateTransform();
    }

    public void updateTransform() {
        Matrix4 localTransform = position.getLocalTransform();
        localTransform.idt().translate(center).rotate(Vector3.Y, yaw).rotate(Vector3.X, pitch).translate(translate)
                .rotate(Vector3.Y, localYaw).rotate(Vector3.X, localPitch);
    }
}
