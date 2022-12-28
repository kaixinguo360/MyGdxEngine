package com.my.world.enhanced.shader.depthmask;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.my.world.enhanced.render.CustomShaderAttribute;

public class DepthMaskAttribute extends CustomShaderAttribute {

    public static final DepthMaskShader depthMaskShader = new DepthMaskShader();

    public final Color color = new Color();

    public DepthMaskAttribute() {
        this(Color.CLEAR);
    }

    public DepthMaskAttribute(Color color) {
        super(depthMaskShader);
        if (color != null) this.color.set(color);
    }

    @Override
    public Attribute copy() {
        return new DepthMaskAttribute(this.color);
    }
}
