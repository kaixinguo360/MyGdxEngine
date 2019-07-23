package com.my.utils.world;

import com.badlogic.gdx.utils.Array;

public abstract class BaseSystem implements System {

    protected final Array<Entity> entities = new Array<>();

    @Override
    public Array<Entity> getEntities() {
        return entities;
    }
}
