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
        EntityBuilder entityBuilder = new EntityBuilder(assetsManager, tmpEntityManager);
        AircraftBuilder aircraftBuilder = new AircraftBuilder(entityBuilder);
        GunBuilder gunBuilder = new GunBuilder(entityBuilder);
        ObjectBuilder objectBuilder = new ObjectBuilder(entityBuilder);

        assetsManager.addAsset("Runway", Prefab.class, Prefab.create(
                objectBuilder.createRunway("Runway", new Matrix4(), null),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Tower", Prefab.class, Prefab.create(
                objectBuilder.createTower("Tower", new Matrix4(), 5),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Bomb", Prefab.class, Prefab.create(
                aircraftBuilder.createBomb("Bomb", new Matrix4(), null),
                LoadUtil.loaderManager,
                assetsManager,
                systemManager,
                tmpEntityManager
        ));

        assetsManager.addAsset("Bullet", Prefab.class, Prefab.create(
                gunBuilder.createBullet("Bullet", new Matrix4(), null),
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
