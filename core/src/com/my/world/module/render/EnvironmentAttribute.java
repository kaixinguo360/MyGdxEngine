package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.my.world.module.common.ActivatableComponent;

public abstract class EnvironmentAttribute extends ActivatableComponent {
    public abstract Attribute getAttribute();
}
