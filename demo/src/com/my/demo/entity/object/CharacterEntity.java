package com.my.demo.entity.object;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.demo.entity.weapon.BulletEntity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.script.EnhancedCharacterController;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

public class CharacterEntity extends EnhancedEntity {

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

    public final GLTFModelInstance render;
    public final EnhancedCharacterController controller;
    public final CharacterSwitcherAgent characterSwitcherAgent;
    public final CameraEntity camera;

    public CharacterEntity(Param p) {

        transform.translate(0, p.height / 2f, 0).rotate(Vector3.Y, 180).mul(p.transform);
        decompose();

        render = addComponent(new GLTFModelInstance(BulletEntity.model));

        controller = addComponent(new EnhancedCharacterController());
        controller.setActive(p.active);
        controller.shape = BulletEntity.body;
        controller.velocity = p.velocity;
        controller.mass = p.mass;
        controller.keyUp = p.keyUp;
        controller.keyDown = p.keyDown;
        controller.keyLeft = p.keyLeft;
        controller.keyRight = p.keyRight;
        controller.keyJump = p.keyJump;

        characterSwitcherAgent = addComponent(new CharacterSwitcherAgent());
        characterSwitcherAgent.characterName = p.characterName;

        camera = new CameraEntity();
        camera.camera.setActive(p.active);
        camera.controller.yawLocked = true;
        camera.controller.yaw = -180;
        camera.controller.yawTarget = -180;
        camera.controller.recoverEnabled = false;
        camera.setParent(this);
        camera.setName("camera");
        addEntity(camera);
    }
}
