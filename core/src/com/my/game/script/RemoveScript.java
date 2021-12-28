package com.my.game.script;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Entity;
import com.my.utils.world.Scene;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.ScriptSystem;

public class RemoveScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    private static final Vector3 TMP_1 = new Vector3();

    private Position position;
    private boolean handleable;

    @Override
    public void start(Scene scene, Entity entity) {
        this.handleable = entity.contain(Position.class);
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (!handleable) return;
        float dst = position.getLocalTransform().getTranslation(TMP_1).dst(0, 0, 0);
        if (dst > 10000) {
            scene.getEntityManager().getBatch().removeEntity(entity.getId());
        }
    }
}
