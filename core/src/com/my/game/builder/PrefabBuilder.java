package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.*;

public class PrefabBuilder {

    public static void initAssets(Engine engine) {
        AssetsManager assetsManager = engine.getAssetsManager();

        // ----- Init Prefabs ----- //

        EntityManager tmpEntityManager = new EntityManager();
        AircraftBuilder aircraftBuilder = new AircraftBuilder(assetsManager, tmpEntityManager);
        GunBuilder gunBuilder = new GunBuilder(assetsManager, tmpEntityManager);
        ObjectBuilder objectBuilder = new ObjectBuilder(assetsManager, tmpEntityManager);
        BulletBuilder bulletBuilder = new BulletBuilder(assetsManager, tmpEntityManager);

        Context context = engine.newContext();
        context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, tmpEntityManager);

        assetsManager.addAsset("Runway", Prefab.class, Prefab.create(
                objectBuilder.createRunway("Runway", new Matrix4(), null),
                context
        ));

        assetsManager.addAsset("Tower", Prefab.class, Prefab.create(
                objectBuilder.createTower("Tower", new Matrix4(), 5),
                context
        ));

        assetsManager.addAsset("Explosion", Prefab.class, Prefab.create(
                bulletBuilder.createExplosion("Explosion", new Matrix4()),
                context
        ));

        assetsManager.addAsset("Bomb", Prefab.class, Prefab.create(
                bulletBuilder.createBomb("Bomb", new Matrix4(), null),
                context
        ));

        assetsManager.addAsset("Bullet", Prefab.class, Prefab.create(
                bulletBuilder.createBullet("Bullet", new Matrix4(), null),
                context
        ));

        assetsManager.addAsset("Aircraft", Prefab.class, Prefab.create(
                aircraftBuilder.createAircraft("Aircraft", new Matrix4(), 4000, 40),
                context
        ));

        assetsManager.addAsset("Gun", Prefab.class, Prefab.create(
                gunBuilder.createGun("Gun", null, new Matrix4()),
                context
        ));
    }
}
