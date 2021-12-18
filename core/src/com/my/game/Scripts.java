package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Script;

public class Scripts {

    private static final Vector3 TMP_1 = new Vector3();

    public static class RemoveScript extends Script implements Script.OnInit, Script.OnUpdate {

        private Position position;
        private boolean handleable;

        @Override
        public void init(World world, Entity entity) {
            this.handleable = entity.contain(Position.class);
            this.position = entity.getComponent(Position.class);
        }

        @Override
        public void update(World world, Entity entity) {
            if (!handleable) return;
            float dst = position.transform.getTranslation(TMP_1).dst(0, 0, 0);
            if (dst > 10000) {
                world.getEntityManager().getBatch().removeEntity(entity.getId());
            }
        }
    }

    public static class ExitScript extends Script implements Script.OnKeyDown {
        @Override
        public void keyDown(World world, Entity entity, int keycode) {
            if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
        }
    }
}
