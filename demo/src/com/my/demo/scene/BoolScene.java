package com.my.demo.scene;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.object.RunwayEntity;
import com.my.demo.entity.object.TowerEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.entity.CutterEntity;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.render.model.GLTFModel;

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

        character.controller.velocity = 50f;
        Model model = new GLTFModel("bool/cutter.gltf").sceneAsset.scene.model;

        CutterEntity cutter1 = CutterEntity.get(model);
        cutter1.cutterScript.filter = entity -> ("Box".equals(entity.getName()) || "Brick".equals(entity.getName()));
        cutter1.setParent(character.camera);
        cutter1.addToScene(scene);

        CutterEntity cutter2 = CutterEntity.get(model);
        cutter2.cutterScript.filter = entity -> ("Box".equals(entity.getName()) || "Brick".equals(entity.getName()));
        cutter2.position.setLocalTransform(m -> m.rotate(Vector3.Z, 90));
        cutter2.setParent(character.camera);
        cutter2.addToScene(scene);

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        guiEntity.addComponent((InputSystem.OnTouchDown) (screenX, screenY, pointer, button) -> {
            if (character.camera.camera.isActive()) {
                if (button == Input.Buttons.LEFT) {
                    cutter1.cutterScript.doCut();
                }
                if (button == Input.Buttons.RIGHT) {
                    cutter2.cutterScript.doCut();
                }
            }
        });
        scene.addEntity(guiEntity);

        return ground;
    }
}
