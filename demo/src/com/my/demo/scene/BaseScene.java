package com.my.demo.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.aircraft.AircraftEntity;
import com.my.demo.entity.common.CharacterSwitcher;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.demo.entity.common.WeaponSwitcher;
import com.my.demo.entity.gun.GunEntity;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.object.CharacterEntity;
import com.my.demo.entity.weapon.RocketEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.entity.ChoppingEntity;
import com.my.world.enhanced.bool.entity.RigidBodyCutterEntity;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.entity.RenderEntity;
import com.my.world.enhanced.entity.RigidBodyEntity;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.AnimationChannel;
import com.my.world.module.common.EnhancedPosition;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.script.ScriptSystem;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.ArrayList;
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

    public WeaponSwitcher switcher;

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

        // Create WeaponSwitcher
        switcher = character.addComponent(new WeaponSwitcher());
        switcher.setActive(false);
        switcher.add(buildChoppingEntity(scene));
        switcher.add(buildRigidBodyCutterEntity(scene));
        switcher.add(buildRocketLauncher(scene));

        return ground;
    }

    private String buildChoppingEntity(Scene scene) {
        Model model = new GLTFModel("bool/cutter.gltf").sceneAsset.scene.model;
        Material material = new Material(PBRColorAttribute.createDiffuse(Color.WHITE), new BlendingAttribute(true, 0.3f));
        long attributes = VertexAttributes.Usage.Position;

        ChoppingEntity choppingEntity = new ChoppingEntity(model);
        choppingEntity.setName("ChoppingEntity");
//        choppingEntity.cutter.position.setLocalTransform(m -> m.setToTranslation(0.25f, -0.25f, -0.5f).rotate(Vector3.Y, 5));
        choppingEntity.detector.detectorScript.filter = entity -> ("Box".equals(entity.getName()) || "Brick".equals(entity.getName()));
        choppingEntity.setParent(character.camera);
        choppingEntity.addToScene(scene);

        RenderEntity cutterRenderEntity = new RenderEntity(new Box(10f, 0.0001f, 10f, material, attributes));
        cutterRenderEntity.render.setActive(false);
        cutterRenderEntity.addComponent((InputSystem.OnTouchDown) (screenX, screenY, pointer, button) -> {
            if (button == choppingEntity.choppingScript.choppingButton) {
                cutterRenderEntity.render.setActive(true);
                character.controller.yawRate = 0.1f;
                character.camera.controller.pitchRate = 0.1f;
            }
        });
        cutterRenderEntity.addComponent((InputSystem.OnTouchUp) (screenX, screenY, pointer, button) -> {
            if (button == choppingEntity.choppingScript.choppingButton) {
                cutterRenderEntity.render.setActive(false);
                character.controller.yawRate = 1;
                character.camera.controller.pitchRate = 1;
            }
        });
        cutterRenderEntity.setParent(choppingEntity.cutter);
        cutterRenderEntity.addToScene(scene);

        return choppingEntity.getName();
    }

    private String buildRigidBodyCutterEntity(Scene scene) {
        RigidBodyCutterEntity entity = RigidBodyCutterEntity.sphere(10, 10, 12);
        entity.setName("RigidBodyCutterEntity");
        entity.position.setLocalTransform(m -> m.setToTranslation(0, 0, -10));
        entity.detectorScript.filter = e -> ("Box".equals(e.getName()) || "Brick".equals(e.getName()));
        entity.cutterScript.type = BooleanUtil.Type.DIFF;
        entity.render.setActive(false);
        Animation animation = entity.addComponent(new Animation(new AnimationChannel() {{
            Vector3 targetV = new Vector3(1, 1, 1);
            Vector3 currentV = new Vector3();
            float duration = 2;
            component = EnhancedPosition.class;
            field = "scale";
            values = time -> currentV.setZero().lerp(targetV, MathUtils.clamp(time / duration, 0.01f, 1));
        }}));
        entity.addComponent((InputSystem.OnTouchDown) (screenX, screenY, pointer, button) -> {
            if (button == Input.Buttons.LEFT) {
                animation.setCurrentTime(0);
                animation.setActive(true);
                entity.render.setActive(true);
            }
        });
        entity.addComponent((InputSystem.OnTouchUp) (screenX, screenY, pointer, button) -> {
            if (button == Input.Buttons.LEFT) {
                animation.setActive(false);
                entity.render.setActive(false);
                entity.cutterScript.doCut(entity.detectorScript.getEntities(new ArrayList<>()));
            }
        });
        entity.setParent(character.camera);
        entity.addToScene(scene);
        return entity.getName();
    }

    private String buildRocketLauncher(Scene scene) {
        EnhancedEntity entity = new EnhancedEntity();
        entity.setName("RocketLauncher");
        Vector3 impulse = new Vector3(0, 1500, 0);
        PhysicsSystem physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
        entity.addComponent((ScriptSystem.OnUpdate) (s, e) -> {
            if (s.getTimeManager().getFrameCount() % 10 != 0) return;
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                Entity target = physicsSystem.pick(character.camera.camera.getCamera(), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 5000);
                if (target == null) return;
                RocketEntity.createRocket(m -> {
                    m.set(entity.position.getGlobalTransform());
                    m.translate(0, 0, -0.25f);
                    m.rotate(Vector3.Z, MathUtils.random(-60, 60));
                    m.translate(0, 1.5f, 0);
                    m.rotate(Vector3.X, -90);
                    m.rotate(Vector3.X, 40);
                }, target.getId(), 100, 180, impulse).addToScene(scene);
            }
        });
        entity.setParent(character.camera);
        entity.addToScene(scene);
        return entity.getName();
    }
}
