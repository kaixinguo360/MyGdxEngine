package com.my.demo.entity.tool;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.SightScript;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.weapon.BombEntity;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;

public class BombTool extends EnhancedEntity {

    public BombTool(CameraEntity camera) {
        setName("BombTool");
        EmitterToolScript script = addComponent(new EmitterToolScript());
        script.parentId = camera.getId();
        script.offset = new Matrix4().translate(0, 1, -2).rotate(Vector3.X, -90);
        script.impulse = new Vector3(0, 5000, 0);
        script.builder = (EntityGenerator) BombEntity::new;
        script.burstMode = false;
        script.RoF = 0;
        addComponent(new SightScript());
        setParent(camera);
    }

}
