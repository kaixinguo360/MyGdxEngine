package com.my.demo.builder.scene.test;

import com.badlogic.gdx.Input;
import com.my.demo.builder.BaseBuilder;
import com.my.demo.builder.object.CharacterBuilder;
import com.my.demo.builder.object.GroundBuilder;
import com.my.demo.builder.test.AnimationBuilder;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.DefaultAnimationController;
import com.my.world.module.input.InputSystem;

import java.util.Map;

public class TestSceneBuilder extends BaseBuilder<TestSceneBuilder> {

    public GroundBuilder groundBuilder;
    public CharacterBuilder characterBuilder;
    public AnimationBuilder animationBuilder;

    @Override
    protected void initDependencies() {
        groundBuilder = getDependency(GroundBuilder.class);
        characterBuilder = getDependency(CharacterBuilder.class);
        animationBuilder = getDependency(AnimationBuilder.class);
    }

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        Entity ground = groundBuilder.build(scene, null);

        characterBuilder.build(scene);

        Entity animationEntity = animationBuilder.build(scene);
        scene.addEntity(newEntity((InputSystem.OnKeyDown) keycode -> {
            if (keycode == Input.Keys.R) {
                Animation animation = animationEntity.getComponent(Animation.class);
                ((DefaultAnimationController) animation.animationController).initState = "state2";
            }
        }));

        return ground;
    }

    public static Entity newEntity(Component component) {
        Entity entity = new Entity();
        entity.addComponent(component);
        return entity;
    }
}
