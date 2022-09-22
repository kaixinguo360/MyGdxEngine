package com.my.demo.scene;

import com.badlogic.gdx.math.Matrix4;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.object.RunwayEntity;
import com.my.demo.entity.object.TowerEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.entity.CutterEntity;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;

import java.util.Map;

public class BoolScene extends BaseScene<BoolScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        super.build(scene, params);

        RunwayEntity runway = new RunwayEntity();
        runway.addToScene(scene);

        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                TowerEntity tower = new TowerEntity();
                tower.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
                tower.addToScene(scene);
            }
        }

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        character.controller.velocity = 50f;
        Matrix4 offset = new Matrix4().setToTranslation(-25, 0, 0);
        character.addComponent((InputSystem.OnTouchDown) (int screenX, int screenY, int pointer, int button) -> {
            CutterEntity cutter = new CutterEntity(50, 50, 50, offset);
            cutter.position.setGlobalTransform(m -> character.position.getGlobalTransform(m));
            cutter.addToScene(scene);
        });

        return ground;
    }
}
