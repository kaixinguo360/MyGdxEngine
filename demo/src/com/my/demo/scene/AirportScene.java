package com.my.demo.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.aircraft.AircraftEntity;
import com.my.demo.entity.aircraft.AircraftScript;
import com.my.demo.entity.common.CharacterSwitcher;
import com.my.demo.entity.common.CharacterSwitcherAgent;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.gun.GunEntity;
import com.my.demo.entity.object.*;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.render.light.GLTFSpotLight;

import java.util.Map;

public class AirportScene extends BaseBuilder<AirportScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        GroundEntity ground = new GroundEntity();
        ground.addToScene(scene);

        RunwayEntity runway = new RunwayEntity();
        runway.addToScene(scene);

        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                TowerEntity tower = new TowerEntity();
                tower.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
                tower.addToScene(scene);
            }
        }

        int aircraftNum = 0;
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    int finalAircraftNum = aircraftNum;
                    Matrix4 transform = new Matrix4().setToTranslation(x, y, z);
                    AircraftEntity aircraft = new AircraftEntity();
                    aircraft.setName("Aircraft-" + finalAircraftNum);
                    aircraft.transform.set(transform);
                    aircraft.removeComponent(AircraftScript.class);
                    aircraft.addToScene(scene);
                    aircraftNum++;
                }
            }
        }

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
                .build());
        character.addToScene(scene);

        Entity spotLight = new Entity();
        spotLight.setName("spotLight");
        spotLight.addComponent(new Position(new Matrix4().translate(0, 1, 0)));
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

        return ground;
    }
}
