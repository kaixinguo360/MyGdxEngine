package com.my.demo.scene;

import com.badlogic.gdx.Gdx;
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
import com.my.demo.entity.tool.*;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.Box;
import com.my.world.module.script.ScriptSystem;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Map;

public class BaseScene<T extends BaseScene<T>> extends BaseBuilder<T> {

    public EnhancedEntity ground;

    public AircraftEntity aircraft;
    public CameraEntity aircraftCamera;

    public GunEntity gun;
    public CameraEntity gunCamera;

    public CharacterEntity character;

    public Entity characterSelectorEntity;
    public CharacterSwitcher characterSwitcher;

    public ToolSwitcher switcher;

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
        ground.setName("ground");
        ground.addToScene(scene);

        // Create Aircraft
        aircraft = new AircraftEntity();
        aircraft.setName("Aircraft-6");
        aircraft.transform.setToTranslation(0, 0, 200);
        aircraft.addComponent(new CharacterSwitcherAgent()).characterName = "aircraft";
        aircraft.addToScene(scene);
        aircraftCamera = new CameraEntity();
        aircraftCamera.setName("camera");
        aircraftCamera.setParent(aircraft.findChildByName("body"));
        aircraftCamera.controller.translateTarget.set(0, 0.8f, -1.5f);
        aircraftCamera.addToScene(scene);

        // Create Gun
        gun = new GunEntity(ground);
        gun.setName("Gun-0");
        gun.transform.setToTranslation(0, 0.01f / 2, -20);
        gun.decompose();
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform(new Matrix4());
        gun.rotateY.constraint.frameInA.set(groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90));
        gun.rotateY.constraint.frameInB.setToRotation(Vector3.X, 90);
        gun.rotateY.constraint.useLinearReferenceFrameA = false;
        gun.addToScene(scene);

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
        gun.gunScript.targetEntity = character.getId();

        // Create CharacterSwitcher
        characterSelectorEntity = new Entity();
        characterSelectorEntity.setName("characterSelectorEntity");
        characterSwitcher = characterSelectorEntity.addComponent(new CharacterSwitcher());
        characterSwitcher.characterNames.add("aircraft");
        characterSwitcher.characterNames.add("character");
        scene.addEntity(characterSelectorEntity);

        // Create ToolSwitcher
        switcher = character.addComponent(new ToolSwitcher());
        switcher.setActive(false);
        addWeapon(scene, new PickTool(character.camera));
        addWeapon(scene, new MissileTool(character.camera));
        addWeapon(scene, new BombTool(character.camera));
        addWeapon(scene, new BulletTool(character.camera));
        addWeapon(scene, new EnhancedEntity() {{
            setName("TmpTool");
            int interval = 5;
            Vector3 impulse = new Vector3(0, 1500, 0);
            PhysicsSystem physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
            addComponent((ScriptSystem.OnUpdate) (s, e) -> {
                if (s.getTimeManager().getFrameCount() % interval != 0) return;
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    Entity target = physicsSystem.pick(character.camera.camera.getCamera(), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 5000);
                    if (target == null) return;
                    gun.gunScript.targetEntity = target.getId();
                }
            });
        }});

        return ground;
    }

    private void addWeapon(Scene scene, EnhancedEntity entity) {
        entity.addToScene(scene);
        switcher.add(entity.getName());
    }
}
