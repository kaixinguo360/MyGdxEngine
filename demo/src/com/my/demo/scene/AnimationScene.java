package com.my.demo.scene;

import com.badlogic.gdx.Input;
import com.my.demo.entity.common.AnimationEntity;
import com.my.demo.entity.object.CharacterEntity;
import com.my.demo.entity.object.GroundEntity;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.DefaultAnimationController;
import com.my.world.module.input.InputSystem;

import java.util.Map;

public class AnimationScene extends BaseBuilder<AnimationScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        GroundEntity ground = new GroundEntity();
        ground.addToScene(scene);

        CharacterEntity character = new CharacterEntity(new CharacterEntity.Param());
        character.addToScene(scene);

        AnimationEntity animationEntity = new AnimationEntity();
        animationEntity.addToScene(scene);

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
