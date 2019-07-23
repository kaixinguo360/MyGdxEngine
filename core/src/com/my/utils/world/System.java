package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public interface System extends Disposable {
    boolean check(Entity entity);
    Array<Entity> getEntities();
}
