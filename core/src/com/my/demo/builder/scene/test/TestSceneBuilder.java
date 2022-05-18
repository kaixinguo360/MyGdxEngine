package com.my.demo.builder.scene.test;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.BaseBuilder;
import com.my.demo.builder.object.CharacterBuilder;
import com.my.demo.builder.object.GroundBuilder;
import com.my.demo.builder.test.AnimationBuilder;
import com.my.demo.builder.test.DepthMaskEntity;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.animation.Animation;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.model.Sphere;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

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

        Entity character = characterBuilder.build(scene);

        Entity animationEntity = animationBuilder.build(scene);
        scene.addEntity(newEntity((InputSystem.OnKeyDown) keycode -> {
            if (keycode == Input.Keys.R) {
                Animation animation = animationEntity.getComponent(Animation.class);
                animation.animationController.initState = "state2";
            }
        }));

        DepthMaskEntity depthMask = new DepthMaskEntity(1f, 2f);
        depthMask.setName("depthMask");
        depthMask.position.getLocalTransform().setToTranslation(0, 0, 20).rotate(Vector3.X, 90);
        depthMask.addToScene(scene);

        Entity entity = new Entity();
        entity.addComponent(new Position(new Matrix4().translate(0, 2.5f, 0)));
        float radius = 0.5f;
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.RED));
        entity.addComponent(new Sphere(2 * radius, 2 * radius, 2 * radius, 16, 16, material, VertexAttributes.Usage.Position));
        SphereBody rigidBody = entity.addComponent(new SphereBody(radius, 0));
        rigidBody.isKinematic = true;
        rigidBody.autoConvertToWorldTransform = false;
        entity.setParent(character);
        scene.addEntity(entity);

        return ground;
    }

    public static Entity newEntity(Component component) {
        Entity entity = new Entity();
        entity.addComponent(component);
        return entity;
    }
}
