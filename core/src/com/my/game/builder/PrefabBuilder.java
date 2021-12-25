package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.game.LoadUtil;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.EntityManager;
import com.my.utils.world.Prefab;
import com.my.utils.world.SystemManager;

public class PrefabBuilder {

    public static void initAssets(AssetsManager assetsManager, SystemManager systemManager) {

        // ----- Init Prefabs ----- //

        EntityManager tmpEntityManager = new EntityManager();
        AircraftBuilder aircraftBuilder = new AircraftBuilder(assetsManager, tmpEntityManager);
        GunBuilder gunBuilder = new GunBuilder(assetsManager, tmpEntityManager);
        SceneBuilder sceneBuilder = new SceneBuilder(assetsManager, tmpEntityManager);
        BulletBuilder bulletBuilder = new BulletBuilder(assetsManager, tmpEntityManager);

        assetsManager.addAsset("Runway", Prefab.class, Prefab.create(
                sceneBuilder.createRunway("Runway", new Matrix4(), null),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Tower", Prefab.class, Prefab.create(
                sceneBuilder.createTower("Tower", new Matrix4(), 5),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Explosion", Prefab.class, Prefab.create(
                bulletBuilder.createExplosion("Explosion", new Matrix4()),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Bomb", Prefab.class, Prefab.create(
                bulletBuilder.createBomb("Bomb", new Matrix4(), null),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Bullet", Prefab.class, Prefab.create(
                bulletBuilder.createBullet("Bullet", new Matrix4(), null),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Aircraft", Prefab.class, Prefab.create(
                aircraftBuilder.createAircraft("Aircraft", new Matrix4(), 4000, 40),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Gun", Prefab.class, Prefab.create(
                gunBuilder.createGun("Gun", null, new Matrix4()),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));
    }
}
