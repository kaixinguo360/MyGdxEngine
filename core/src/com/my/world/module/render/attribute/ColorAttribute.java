package com.my.world.module.render.attribute;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.render.EnvironmentAttribute;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ColorAttribute extends EnvironmentAttribute implements Loadable.OnInit {

    @Config public long type;
    @Config public Color color;

    private com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute attribute;

    public ColorAttribute(long type, Color color) {
        this.type = type;
        this.color = color;
        init();
    }

    @Override
    public void init() {
        attribute = new com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute(type, color);
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }
}
