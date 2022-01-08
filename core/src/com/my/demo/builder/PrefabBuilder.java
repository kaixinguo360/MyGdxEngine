package com.my.demo.builder;

import com.my.world.core.Engine;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;

import java.util.function.Function;

public class PrefabBuilder {

    public static void initAssets(Engine engine) {
        Scene scene = engine.getSceneManager().newScene("prefab");

        PrefabBuilder.createPrefab(scene, ObjectBuilder::createRunway);
        PrefabBuilder.createPrefab(scene, ObjectBuilder::createTower);
        PrefabBuilder.createPrefab(scene, BulletBuilder::createExplosion);
        PrefabBuilder.createPrefab(scene, BulletBuilder::createBomb);
        PrefabBuilder.createPrefab(scene, BulletBuilder::createBullet);
        PrefabBuilder.createPrefab(scene, AircraftBuilder::createAircraft);
        PrefabBuilder.createPrefab(scene, GunBuilder::createGun);

        engine.getSceneManager().removeScene(scene.getId());
    }

    public static  void createPrefab(Scene scene, Function<Scene, String> function) {
        String name = function.apply(scene);
        Prefab prefab = scene.dumpToPrefab();
        scene.getEngine().getAssetsManager().addAsset(name, Prefab.class, prefab);
    }
}
