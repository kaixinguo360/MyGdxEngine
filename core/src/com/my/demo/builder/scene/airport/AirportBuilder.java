package com.my.demo.builder.scene.airport;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.BaseBuilder;
import com.my.demo.builder.aircraft.AircraftBuilder;
import com.my.demo.builder.common.CharacterSwitcher;
import com.my.demo.builder.common.CharacterSwitcherAgent;
import com.my.demo.builder.common.GUIScript;
import com.my.demo.builder.gun.GunBuilder;
import com.my.demo.builder.object.*;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.render.light.GLTFSpotLight;

import java.util.HashMap;
import java.util.Map;

public class AirportBuilder extends BaseBuilder<AirportBuilder> {

    public GroundBuilder groundBuilder;
    public RunwayBuilder runwayBuilder;
    public TowerBuilder towerBuilder;
    public AircraftBuilder aircraftBuilder;
    public GunBuilder gunBuilder;
    public CharacterBuilder characterBuilder;
    public CameraBuilder cameraBuilder;

    @Override
    protected void initDependencies() {
        groundBuilder = getDependency(GroundBuilder.class);
        runwayBuilder = getDependency(RunwayBuilder.class);
        towerBuilder = getDependency(TowerBuilder.class);
        aircraftBuilder = getDependency(AircraftBuilder.class);
        gunBuilder = getDependency(GunBuilder.class);
        characterBuilder = getDependency(CharacterBuilder.class);
        cameraBuilder = getDependency(CameraBuilder.class);
    }

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        Entity ground = groundBuilder.build(scene, null);

        runwayBuilder.build(scene, null);

        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                Entity entity = towerBuilder.build(scene, null);
                entity.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
            }
        }

        int aircraftNum = 0;
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    int finalAircraftNum = aircraftNum;
                    Matrix4 transform = new Matrix4().setToTranslation(x, y, z);
                    aircraftBuilder.build(scene, new HashMap<String, Object>() {{
                        put("Aircraft.config.components[0].config.localTransform", transform);
                        put("Aircraft.config.components[1]", null);
                        put("Aircraft.config.name", "Aircraft-" + finalAircraftNum);
                    }});
                    aircraftNum++;
                }
            }
        }

        Entity aircraftEntity = aircraftBuilder.build(scene, new HashMap<String, Object>() {{
            put("Aircraft.config.components[0].config.localTransform", new Matrix4().setToTranslation(0, 0, 200));
            put("Aircraft.config.name", "Aircraft-6");
        }});
        aircraftEntity.addComponent(new CharacterSwitcherAgent()).characterName = "aircraft";
        cameraBuilder.build(scene, new HashMap<String, Object>() {{
            put("Camera.config.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.config.parent", aircraftEntity.findChildByName("body"));
            put("Camera.config.name", "camera");
        }});

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        Entity gunEntity = gunBuilder.build(scene, new HashMap<String, Object>() {{
            put("Gun.config.components[0].config.localTransform", new Matrix4().setToTranslation(0, 0.01f / 2, -20));
            put("Gun.config.components[1].config.active", false);
            put("Gun.config.name", "Gun-0");
        }});
        gunEntity.addComponent(new CharacterSwitcherAgent()).characterName = "gun";
        Entity rotateY = gunEntity.findChildByName("rotate_Y");
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform(new Matrix4());
        rotateY.addComponent(new HingeConstraint(ground,
                groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90),
                new Matrix4().rotate(Vector3.X, 90),
                false
        ));
        cameraBuilder.build(scene, new HashMap<String, Object>() {{
            put("Camera.config.components[2].config.active", false);
            put("Camera.config.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.config.parent", gunEntity.findChildByName("barrel"));
            put("Camera.config.name", "camera");
        }});

        Entity character = characterBuilder.build(scene, CharacterBuilder.Param.builder()
                .active(false)
                .characterName("character")
                .build());

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
