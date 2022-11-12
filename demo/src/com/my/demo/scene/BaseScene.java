package com.my.demo.scene;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.aircraft.AircraftEntity;
import com.my.demo.entity.common.CharacterSwitcher;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.demo.entity.gun.GunEntity;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.object.CharacterEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.enhanced.render.CameraGroup;
import com.my.world.enhanced.util.EntityMultiplexer;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.Box;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Map;

public class BaseScene<T extends BaseScene<T>> extends BaseBuilder<T> {

    public EnhancedEntity ground;

    public AircraftEntity aircraft;
    public EntityMultiplexer entityMultiplexer;
    public CameraGroup cameraGroup;
    public CameraEntity aircraftCamera1;
    public CameraEntity aircraftCamera2;
    public CameraEntity aircraftCamera3;

    public GunEntity gun;
    public CameraEntity gunCamera;

    public CharacterEntity character;

    public Entity characterSelectorEntity;
    public CharacterSwitcher characterSwitcher;

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {

        // Create Ground
        ground = new RigidBodyEntity(
                new Box(10000, 100, 20000,
                        new Material(PBRColorAttribute.createBaseColorFactor(Color.WHITE)),
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal),
                new BoxBody(new Vector3(5000,50,10000), 0f)
        );
        ground.position.setLocalTransform(m -> m.translate(0, -50, 0));
        ground.addToScene(scene);

        // Create Aircraft
        aircraft = new AircraftEntity();
        aircraft.setName("Aircraft-6");
        aircraft.transform.setToTranslation(0, 0, 200);
        aircraft.addComponent(new CharacterSwitcherAgent()).characterName = "aircraft";
        entityMultiplexer = aircraft.addComponent(new EntityMultiplexer());
        entityMultiplexer.add("camera1");
        entityMultiplexer.add("camera2");
        entityMultiplexer.add("camera3");
        cameraGroup = aircraft.addComponent(new CameraGroup());
        cameraGroup.cameras.add("camera1");
        cameraGroup.cameras.add("camera2");
        cameraGroup.cameras.add("camera3");
        aircraft.addComponent((InputSystem.OnKeyDown) keycode -> {
            if (keycode == Input.Keys.SHIFT_LEFT) {
                entityMultiplexer.next();
                cameraGroup.nextCamera();
            }
        });
        aircraft.addToScene(scene);

        aircraftCamera1 = new CameraEntity();
        aircraftCamera1.setName("camera1");
        aircraftCamera1.setParent(aircraft.findChildByName("body"));
        aircraftCamera1.position.setLocalTransform(m -> m.setToTranslation(0, 0.8f, -1.5f));
        aircraftCamera1.controller.translateTarget.set(0, 0.8f, -1.5f);
        aircraftCamera1.addToScene(scene);
        aircraftCamera2 = new CameraEntity();
        aircraftCamera2.setName("camera2");
        aircraftCamera2.setParent(aircraft.findChildByName("body"));
        aircraftCamera1.position.setLocalTransform(m -> m.setToTranslation(0, 5.8f, 20 - 1.5f));
        aircraftCamera2.controller.translateTarget.set(0, 0, 20);
        aircraftCamera2.controller.localPitchTarget = 0;
        aircraftCamera2.controller.centerTarget.set(0, 5.8f, -1.5f);
        aircraftCamera2.addToScene(scene);
        aircraftCamera3 = new CameraEntity();
        aircraftCamera3.setName("camera3");
        aircraftCamera3.setParent(aircraft.findChildByName("body"));
        aircraftCamera1.position.setLocalTransform(m -> m.setToTranslation(0, -20, 50 - 1.5f));
        aircraftCamera3.controller.translateTarget.set(0, 0, 50);
        aircraftCamera3.controller.localPitchTarget = 0;
        aircraftCamera3.controller.centerTarget.set(0, -20, -1.5f);
        aircraftCamera3.addToScene(scene);

        // Create Gun
        gun = new GunEntity(ground);
        gun.setName("Gun-0");
        gun.transform.setToTranslation(0, 0.01f / 2, -20);
        gun.decompose();
        gun.gunScript.setActive(false);
        gun.addComponent(new CharacterSwitcherAgent()).characterName = "gun";
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform(new Matrix4());
        gun.rotateY.constraint.frameInA.set(groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90));
        gun.rotateY.constraint.frameInB.setToRotation(Vector3.X, 90);
        gun.rotateY.constraint.useLinearReferenceFrameA = false;
        gun.addToScene(scene);
        gunCamera = new CameraEntity();
        gunCamera.setName("camera");
        gunCamera.setParent(gun.barrel);
        gunCamera.controller.translateTarget.set(0, 0.8f, -1.5f);
        gunCamera.camera.setActive(false);
        gunCamera.addToScene(scene);

        // Create Character
        character = new CharacterEntity(CharacterEntity.Param.builder()
                .active(false)
                .height(1.6f)
                .characterName("character")
                .velocity(4f)
                .build());
        character.position.setLocalTransform(m -> m.setToTranslation(0, 2, 0));
        character.controller.maxVelocity = 100;
        character.controller.minVelocity = 1;
        character.controller.acceleration = 25;
        character.controller.damping = 100;
        character.controller.jumpCD = 0;
        character.addToScene(scene);

        // Create CharacterSwitcher
        characterSelectorEntity = new Entity();
        characterSelectorEntity.setName("characterSelectorEntity");
        characterSwitcher = characterSelectorEntity.addComponent(new CharacterSwitcher());
        characterSwitcher.characterNames.add("aircraft");
        characterSwitcher.characterNames.add("gun");
        characterSwitcher.characterNames.add("character");
        scene.addEntity(characterSelectorEntity);

        return ground;
    }

    public void registerCharacter(String name) {
        characterSwitcher.characterNames.add(name);
    }
}
