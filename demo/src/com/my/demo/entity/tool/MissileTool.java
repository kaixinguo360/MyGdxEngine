package com.my.demo.entity.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.SightScript;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.weapon.MissileEntity;
import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.camera.Camera;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.script.ScriptSystem;

public class MissileTool extends EnhancedEntity {

    public static final int interval = 5;
    public static final Vector3 impulse = new Vector3(0, 1500, 0);

    public MissileTool(CameraEntity camera) {
        setName("MissileTool");
        String cameraId = camera.getId();
        addComponent((ScriptSystem.OnUpdate) (s, e) -> {
            if (s.getTimeManager().getFrameCount() % interval != 0) return;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                PhysicsSystem physicsSystem = s.getSystemManager().getSystem(PhysicsSystem.class);
                Entity c = s.getEntityManager().findEntityById(cameraId);
                Entity target = physicsSystem.pick(c.getComponent(Camera.class).getCamera(), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 5000);
                if (target == null) return;
                MissileEntity.createMissile(m -> {
                    m.set(e.getComponent(Position.class).getGlobalTransform());
                    m.translate(0, 0, -0.25f);
                    m.rotate(Vector3.Z, MathUtils.random(-60, 60));
                    m.translate(0, 1.5f, 0);
                    m.rotate(Vector3.X, -90);
                    m.rotate(Vector3.X, 40);
                }, target.getId(), 100, 180, impulse).addToScene(s);
            }
        });
        addComponent(new SightScript());
        setParent(camera);
    }
}
