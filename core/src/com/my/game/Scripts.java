package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Script;

public class Scripts {

    private static final Vector3 TMP_1 = new Vector3();

    public static class RemoveScript extends Script {

        private Position position;
        private boolean handleable;

        @Override
        public void init(World world, Entity entity) {
            this.handleable = entity.contain(Position.class);
            this.position = entity.getComponent(Position.class);
        }

        @Override
        public void execute(World world, Entity entity) {
            if (!handleable) return;
            float dst = position.transform.getTranslation(TMP_1).dst(0, 0, 0);
            if (dst > 10000) {
                world.getEntityManager().getBatch().removeEntity(entity.getId());
            }
        }
    }
}
