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
import com.my.world.module.render.light.GLTFSpotLight;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import java.util.Map;

public class RandomHouseScene extends BaseBuilder<HouseScene> {

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
                .velocity(5)
                .build());
        character.position.setLocalTransform(m -> m.setToTranslation(0, 2, -50f));
        character.camera.controller.translate.set(0, 0, 0.001f);
        character.camera.controller.center.set(0, 0.5f, 0);
        character.controller.jumpCD = 0;
        character.addToScene(scene);

        Entity spotLight = new Entity();
        spotLight.setName("spotLight");
        spotLight.addComponent(new Position(new Matrix4().translate(0, 0.5f, 0)));
        spotLight.addComponent(new GLTFSpotLight(Color.WHITE.cpy(), Vector3.Z.cpy(), 10f, 300f, 60f, 30f));
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

        float houseLength = 10f;
        float houseWidth = 10f;
        float houseHeight = 3.5f;

        context.subContext(null, c -> {

            for (int i = 0; i < 10; i++) {
                c.set("楼层"+i+".墙材质", new Material(PBRColorAttribute.createBaseColorFactor(new Color(
                        (float) Math.random(), (float) Math.random(), (float) Math.random(), 1
                ))));
                c.set("楼层"+i+".随机数种子", System.nanoTime());
            }

            for (int i = 0; i < 10; i++) {

                c.set("楼层数", i + 1);
                c.set("楼层高", houseHeight);
                c.set("楼长度", houseLength);
                c.set("楼宽度", houseWidth);
                c.set("楼梯长度", houseHeight * 1.5f);
//                c.set("楼层高", 10f);
//                c.set("房间墙厚度", 1f);
//                c.set("房间最小长度", 10f);

//                c.set("柱子粗细", 1f);
//                c.set("墙厚度", 0.5f);

//                c.set("门宽度", 2f);
//                c.set("门高度", 3f);

                HouseEntity house = new HouseEntity(c);
                int finalI = i;
                house.position.setLocalTransform(m -> m.setToTranslation(
                        finalI * (houseWidth + 3),
                        0.005f,
                        -houseLength / 2 - 50
                ).rotate(Vector3.Y, 180));
                house.addToScene(scene);
            }

            return null;
        });

        return ground;
    }
}
