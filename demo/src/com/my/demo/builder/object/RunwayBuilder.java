package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.common.Position;

import java.util.HashMap;

public class RunwayBuilder extends PrefabBuilder<RunwayBuilder> {

    {
        prefabName = "Runway";
    }

    public BoxBuilder boxBuilder;

    @Override
    protected void initDependencies() {
        boxBuilder = getDependency(BoxBuilder.class);
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Runway");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            boxBuilder.build(scene, new HashMap<String, Object>() {{
                put("Box.config.components[0].config.localTransform", new Matrix4().translate(10, 0.5f, -10 * finalI));
                put("Box.config.parent", entity);
            }});
            boxBuilder.build(scene, new HashMap<String, Object>() {{
                put("Box.config.components[0].config.localTransform", new Matrix4().translate(-10, 0.5f, -10 * finalI));
                put("Box.config.parent", entity);
            }});
        }
    }
}
