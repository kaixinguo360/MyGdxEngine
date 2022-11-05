package com.my.demo.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.object.RunwayEntity;
import com.my.demo.entity.object.TowerEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.entity.ChoppingEntity;
import com.my.world.enhanced.entity.RenderEntity;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.GLTFModel;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

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
                tower.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -50 * i);
                tower.addToScene(scene);
            }
        }

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        character.controller.velocity = 30f;
        Model model = new GLTFModel("bool/cutter.gltf").sceneAsset.scene.model;

        ChoppingEntity choppingEntity = new ChoppingEntity(model);
        choppingEntity.cutter.position.setLocalTransform(m -> m.setToTranslation(0.25f, -0.25f, -0.5f).rotate(Vector3.Y, 5));
        choppingEntity.detector.detectorScript.filter = entity -> ("Box".equals(entity.getName()) || "Brick".equals(entity.getName()));
        choppingEntity.setParent(character.camera);
        choppingEntity.addToScene(scene);

        Material material = new Material(PBRColorAttribute.createDiffuse(Color.WHITE), new BlendingAttribute(true, 0.3f));
        long attributes = VertexAttributes.Usage.Position;
        RenderEntity cutterRenderEntity = new RenderEntity(new Box(10f, 0.0001f, 10f, material, attributes));
        cutterRenderEntity.render.setActive(false);
        cutterRenderEntity.addComponent((InputSystem.OnTouchDown) (screenX, screenY, pointer, button) -> {
            if (button == choppingEntity.choppingScript.choppingButton) cutterRenderEntity.render.setActive(true);
        });
        cutterRenderEntity.addComponent((InputSystem.OnTouchUp) (screenX, screenY, pointer, button) -> {
            if (button == choppingEntity.choppingScript.choppingButton) cutterRenderEntity.render.setActive(false);
        });
        cutterRenderEntity.setParent(choppingEntity.cutter);
        cutterRenderEntity.addToScene(scene);

        return ground;
    }
}
