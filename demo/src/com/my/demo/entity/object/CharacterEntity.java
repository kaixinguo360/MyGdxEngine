package com.my.demo.entity.object;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.script.EnhancedCharacterController;
import com.my.world.module.render.light.GLTFSpotLight;
import com.my.world.module.render.model.Capsule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class CharacterEntity extends EnhancedEntity {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Param {
        @Builder.Default public Matrix4 transform = new Matrix4();
        @Builder.Default public boolean active = true;
        @Builder.Default public float height = 1.8f;
        @Builder.Default public float velocity = 30;
        @Builder.Default public float mass = 0.5f;
        @Builder.Default public int keyUp = Input.Keys.W;
        @Builder.Default public int keyDown = Input.Keys.S;
        @Builder.Default public int keyLeft = Input.Keys.A;
        @Builder.Default public int keyRight = Input.Keys.D;
        @Builder.Default public int keyJump = Input.Keys.SPACE;
        public String characterName;
    }

    public final Capsule render;
    public final EnhancedCharacterController controller;
    public final CharacterSwitcherAgent characterSwitcherAgent;
    public final CameraEntity camera;

    public CharacterEntity(Param p) {

        float scale = p.height / 2f;
        float radius = 0.4f * scale;
        float height = 1.2f * scale;
        float cameraY = 0.6f * scale;

        transform.translate(0, p.height / 2, 0).rotate(Vector3.Y, 180).mul(p.transform);
        decompose();

        render = addComponent(new Capsule(
                radius, p.height, 16,
                new Material(PBRColorAttribute.createBaseColorFactor(Color.WHITE)), ATTRIBUTES
        ));

        controller = addComponent(new EnhancedCharacterController());
        controller.setActive(p.active);
        controller.shape = new CapsuleBody(radius, height, 50f);
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
        camera.controller.translate.set(0, 0, 0);
        camera.controller.center.set(0, cameraY, 0);
        camera.setParent(this);
        camera.setName("camera");
        addEntity(camera);

        EnhancedEntity spotLight = new EnhancedEntity();
        spotLight.setName("spotLight");
        spotLight.position.setLocalTransform(m -> m.setToTranslation(0, cameraY, 0));
        spotLight.addComponent(new GLTFSpotLight(Color.WHITE.cpy(), Vector3.Z.cpy(), 10f, 300f, 60f, 30f));
        spotLight.setParent(this);
        addEntity(spotLight);
    }
}
