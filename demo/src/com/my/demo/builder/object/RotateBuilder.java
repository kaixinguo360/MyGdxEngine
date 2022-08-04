package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.gun.GunController;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.render.model.GLTFModel;

public class RotateBuilder extends PrefabBuilder<RotateBuilder> {

    {
        prefabName = "Rotate";
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Rotate");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/rotate.gltf"));
        entity.addComponent(new CylinderBody(new Vector3(0.5f,0.5f,0.5f), 50f));
        entity.addComponent(
                new HingeConstraint(
                        scene.tmpEntity(),
                        new Matrix4().rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false
                )
        );
        entity.addComponent(new GunController());
        scene.addEntity(entity);
    }
}
