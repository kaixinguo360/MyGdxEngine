package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.*;

public class PrefabBuilder {

    public static void initAssets(Engine engine) {
        AssetsManager assetsManager = engine.getAssetsManager();
        Scene scene = engine.getSceneManager().newScene("prefab");
        EntityManager entityManager = scene.getEntityManager();

        // ----- Init Prefabs ----- //

        AircraftBuilder aircraftBuilder = new AircraftBuilder(assetsManager, entityManager);
        GunBuilder gunBuilder = new GunBuilder(assetsManager, entityManager);
        ObjectBuilder objectBuilder = new ObjectBuilder(assetsManager, entityManager);
        BulletBuilder bulletBuilder = new BulletBuilder(assetsManager, entityManager);

        objectBuilder.createRunway("Runway", new Matrix4(), null);
        assetsManager.addAsset("Runway", Prefab.class, scene.dumpToPrefab());

        objectBuilder.createTower("Tower", new Matrix4(), 5);
        assetsManager.addAsset("Tower", Prefab.class, scene.dumpToPrefab());

        bulletBuilder.createExplosion("Explosion", new Matrix4());
        assetsManager.addAsset("Explosion", Prefab.class, scene.dumpToPrefab());

        bulletBuilder.createBomb("Bomb", new Matrix4(), null);
        assetsManager.addAsset("Bomb", Prefab.class, scene.dumpToPrefab());

        bulletBuilder.createBullet("Bullet", new Matrix4(), null);
        assetsManager.addAsset("Bullet", Prefab.class, scene.dumpToPrefab());

        aircraftBuilder.createAircraft("Aircraft", new Matrix4(), 4000, 40);
        assetsManager.addAsset("Aircraft", Prefab.class, scene.dumpToPrefab());

        gunBuilder.createGun("Gun", null, new Matrix4());
        assetsManager.addAsset("Gun", Prefab.class, scene.dumpToPrefab());

        engine.getSceneManager().removeScene(scene.getId());
    }
}
