package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.my.world.core.Component;

public abstract class EnvironmentAttribute implements Component {
    public abstract Attribute getAttribute();
}
