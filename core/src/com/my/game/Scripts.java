package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.ScriptComponent;
import com.my.utils.world.sys.ScriptSystem;

public class Scripts {

    private static final Vector3 TMP_1 = new Vector3();

    public static void initAssets(AssetsManager assetsManager) {
        assetsManager.addAsset("RemoveScript", ScriptSystem.Script.class, new RemoveScript());
        assetsManager.addAsset("AircraftScript", ScriptSystem.Script.class, new Aircrafts.AircraftScript());
        assetsManager.addAsset("GunScript", ScriptSystem.Script.class, new Guns.GunScript());
    }

    public static class RemoveScript implements ScriptSystem.Script {

        @Override
        public void init(World world, Entity entity, ScriptComponent scriptComponent) {
            Data data = new Data();
            scriptComponent.customObj = data;
            data.handleable = entity.contain(Position.class);
            data.position = entity.getComponent(Position.class);
        }

        @Override
        public void execute(World world, Entity entity, ScriptComponent scriptComponent) {
            Data data = (Data) scriptComponent.customObj;
            if (!data.handleable) return;
            Position position = data.position;
            float dst = position.transform.getTranslation(TMP_1).dst(0, 0, 0);
            if (dst > 10000) {
                world.getEntityManager().getBatch().removeEntity(entity.getId());
            }
        }

        private static class Data {
            private Position position;
            private boolean handleable;
        }
    }
}
