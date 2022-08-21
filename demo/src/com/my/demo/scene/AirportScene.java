package com.my.demo.scene;

import com.badlogic.gdx.math.Matrix4;
import com.my.demo.entity.aircraft.AircraftEntity;
import com.my.demo.entity.aircraft.AircraftScript;
import com.my.demo.entity.common.GUIScript;
import com.my.demo.entity.object.RunwayEntity;
import com.my.demo.entity.object.TowerEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;

import java.util.Map;

public class AirportScene extends BaseScene<AirportScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        super.build(scene, params);

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

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        return ground;
    }
}
