package com.my.demo.attribute;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Shader;

public class CustomShaderAttribute extends Attribute {

    public static final String CustomShaderAlias = "customShader";
    public static final long CustomShader = register(CustomShaderAlias);

    public Shader shader;
    public Object userData;

    public CustomShaderAttribute(Shader shader) {
        this(shader, null);
    }

    public CustomShaderAttribute(Shader shader, Object userData) {
        super(CustomShader);
        this.shader = shader;
        this.userData = userData;
    }

    public <T> T getUserData(Class<T> type) {
        return (T) this.userData;
    }

    @Override
    public Attribute copy() {
        return new CustomShaderAttribute(this.shader, this.userData);
    }

    @Override
    public int compareTo(Attribute o) {
        return 0;
    }
}
