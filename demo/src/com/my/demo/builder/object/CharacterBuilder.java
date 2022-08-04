package com.my.demo.builder.object;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.common.CharacterSwitcherAgent;
import com.my.demo.builder.weapon.BulletBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.ParamBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.script.EnhancedCharacterController;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;

public class CharacterBuilder extends ParamBuilder<CharacterBuilder, CharacterBuilder.Param> {

    public BulletBuilder bulletBuilder;
    public CameraBuilder cameraBuilder;

    @Override
    protected void initDependencies() {
        bulletBuilder = getDependency(BulletBuilder.class);
        cameraBuilder = getDependency(CameraBuilder.class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Param {
        @Builder.Default public Matrix4 transform = new Matrix4();
        @Builder.Default public boolean active = true;
        @Builder.Default public float height = 2f;
        @Builder.Default public float velocity = 30;
        @Builder.Default public float mass = 0.5f;
        @Builder.Default public int keyUp = Input.Keys.W;
        @Builder.Default public int keyDown = Input.Keys.S;
        @Builder.Default public int keyLeft = Input.Keys.A;
        @Builder.Default public int keyRight = Input.Keys.D;
        @Builder.Default public int keyJump = Input.Keys.SPACE;
        public String characterName;
        public GLTFModel model;
        public TemplateRigidBody body;
    }

    @Override
    public Entity build(Scene scene, Param p) {
        GLTFModel model = p.model != null ? p.model : bulletBuilder.model;
        TemplateRigidBody body = p.body != null ? p.body : bulletBuilder.body;

        Entity character = new Entity();
        character.addComponent(new Position(new Matrix4().translate(0, p.height / 2f, 0).rotate(Vector3.Y, 180).mul(p.transform)));
        character.addComponent(new GLTFModelInstance(model));
        EnhancedCharacterController characterController = character.addComponent(new EnhancedCharacterController());
        characterController.setActive(p.active);
        characterController.shape = body;
        characterController.velocity = p.velocity;
        characterController.mass = p.mass;
        characterController.keyUp = p.keyUp;
        characterController.keyDown = p.keyDown;
        characterController.keyLeft = p.keyLeft;
        characterController.keyRight = p.keyRight;
        characterController.keyJump = p.keyJump;
        character.addComponent(new CharacterSwitcherAgent()).characterName = p.characterName;
        scene.addEntity(character);
        cameraBuilder.build(scene, new HashMap<String, Object>() {{
            put("Camera.config.components[2].config.active", p.active);
            put("Camera.config.components[3].config.yawLocked", true);
            put("Camera.config.components[3].config.yaw", -180);
            put("Camera.config.components[3].config.yawTarget", -180);
            put("Camera.config.components[3].config.recoverEnabled", false);
            put("Camera.config.parent", character);
            put("Camera.config.name", "camera");
        }});

        return character;
    }

    @Override
    public Param newParam() {
        return new Param();
    }
}
