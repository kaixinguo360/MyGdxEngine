package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.common.Position;

import java.util.HashMap;

public class TowerBuilder extends PrefabBuilder<TowerBuilder> {

    {
        prefabName = "Tower";
    }

    public WallBuilder wallBuilder;

    @Override
    protected void initDependencies() {
        wallBuilder = getDependency(WallBuilder.class);
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Tower");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        wallBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4());
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-1");
        }});
        wallBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(0, 0, 10).rotate(Vector3.Y, 90));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-2");
        }});
        wallBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(10, 0, 10).rotate(Vector3.Y, 180));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-3");
        }});
        wallBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(10, 0, 0).rotate(Vector3.Y, 270));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-4");
        }});
    }
}
