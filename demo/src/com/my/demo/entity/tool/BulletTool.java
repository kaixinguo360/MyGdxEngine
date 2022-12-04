package com.my.demo.entity.tool;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.SightScript;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.weapon.BulletEntity;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;

public class BulletTool extends EnhancedEntity {

    public BulletTool(CameraEntity camera) {
        setName("BulletTool");
        EmitterToolScript script = addComponent(new EmitterToolScript());
        script.parentId = camera.getId();
        script.offset = new Matrix4().translate(0, 0, -1).rotate(Vector3.X, -90);
        script.velocity = new Vector3(0, 1000, 0);
        script.builder = (EntityGenerator) () -> {
            BulletEntity bullet = new BulletEntity();
            bullet.rigidBody.body.setCcdMotionThreshold(1e-7f);
            return bullet;
        };
        script.burstMode = true;
        script.RoF = 1f / 50;
        addComponent(new SightScript());
        setParent(camera);
    }

}
