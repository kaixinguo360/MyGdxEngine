package com.my.demo.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.aircraft.AircraftEntity;
import com.my.demo.entity.common.CharacterSwitcher;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.gun.GunEntity;
import com.my.demo.entity.house.HouseEntity;
import com.my.demo.entity.object.CameraEntity;
import com.my.demo.entity.object.CharacterEntity;
import com.my.demo.entity.object.GroundEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.FixedConstraint;
import com.my.world.module.render.light.GLTFSpotLight;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Map;

public class HouseScene extends BaseBuilder<HouseScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        GroundEntity ground = new GroundEntity();
        ground.addToScene(scene);

        AircraftEntity aircraft = new AircraftEntity();
        aircraft.setName("Aircraft-6");
        aircraft.transform.setToTranslation(0, 0, 200);
        aircraft.addComponent(new CharacterSwitcherAgent()).characterName = "aircraft";
        aircraft.addToScene(scene);
        CameraEntity aircraftCamera = new CameraEntity();
        aircraftCamera.setName("camera");
        aircraftCamera.setParent(aircraft.findChildByName("body"));
        aircraftCamera.controller.translateTarget.set(0, 0.8f, -1.5f);
        aircraftCamera.addToScene(scene);

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        GunEntity gunEntity = new GunEntity(ground);
        gunEntity.setName("Gun-0");
        gunEntity.transform.setToTranslation(0, 0.01f / 2, -20);
        gunEntity.decompose();
        gunEntity.gunScript.setActive(false);
        gunEntity.addComponent(new CharacterSwitcherAgent()).characterName = "gun";
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform(new Matrix4());
        gunEntity.rotateY.constraint.frameInA.set(groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90));
        gunEntity.rotateY.constraint.frameInB.setToRotation(Vector3.X, 90);
        gunEntity.rotateY.constraint.useLinearReferenceFrameA = false;
        gunEntity.addToScene(scene);

        CameraEntity gunCamera = new CameraEntity();
        gunCamera.setName("camera");
        gunCamera.setParent(gunEntity.barrel);
        gunCamera.controller.translateTarget.set(0, 0.8f, -1.5f);
        gunCamera.camera.setActive(false);
        gunCamera.addToScene(scene);

        CharacterEntity character = new CharacterEntity(CharacterEntity.Param.builder()
                .active(false)
                .characterName("character")
                .velocity(2)
                .build());
        character.position.setLocalTransform(m -> m.setToTranslation(0, 2, -50f));
        character.camera.controller.translate.set(0, 0, 0.001f);
        character.camera.controller.center.set(0, 0.5f, 0);
        character.addToScene(scene);

        Entity spotLight = new Entity();
        spotLight.setName("spotLight");
        spotLight.addComponent(new Position(new Matrix4().translate(0, 0.5f, 0)));
        spotLight.addComponent(new GLTFSpotLight(Color.WHITE.cpy(), Vector3.Z.cpy(), 1f, 10f));
        spotLight.setParent(character);
        scene.addEntity(spotLight);

        Entity characterSelectorEntity = new Entity();
        characterSelectorEntity.setName("characterSelectorEntity");
        CharacterSwitcher characterSwitcher = characterSelectorEntity.addComponent(new CharacterSwitcher());
        characterSwitcher.characterNames.add("aircraft");
        characterSwitcher.characterNames.add("gun");
        characterSwitcher.characterNames.add("character");
        scene.addEntity(characterSelectorEntity);

        EnhancedContext context = EnhancedContext.obtain(null);
        context.setReverse(true);

        context.subContext(null, c -> {
//            c.set("BoxDensity", 160000f);
//            c.set("BreakingImpulseThreshold", 10f);

            c.set("DoorWidth", 2f);
            c.set("DoorHeight", 3f);
            c.set("FrameIsKinematic", true);
            c.set("HoleDetectDepthPadding", 1.2f);

            c.set("HouseFloorsNum", 7);
            c.set("HouseLength", 12f);
            c.set("HouseWidth", 16f);
            c.set("RoomWallThickness", 1f);
            c.set("RoomHeight", 5f);

            c.set("WallThickness", 0.5f);
            c.set("DoorDepth", 0.5f);

            c.set("floor-1.WallMaterial", new Material(PBRColorAttribute.createBaseColorFactor(Color.BLUE)));
            c.set("floor-2.WallMaterial", new Material(PBRColorAttribute.createBaseColorFactor(Color.YELLOW)));
            c.set("floor-3.WallMaterial", new Material(PBRColorAttribute.createBaseColorFactor(Color.PINK)));

            for (int i = 0; i < 10; i++) {
                HouseEntity house = new HouseEntity(c);
                int finalI = i;
                house.position.setLocalTransform(m -> m.setToTranslation(finalI * 20, 0.005f, -50).rotate(Vector3.Y, 180));
                FixedConstraint.connect(ground, house.floors.get(0).floor, 20000000f);
                house.addToScene(scene);
            }

            return null;
        });

        return ground;
    }
}
