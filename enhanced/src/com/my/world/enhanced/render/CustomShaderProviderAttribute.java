package com.my.world.enhanced.render;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class CustomShaderProviderAttribute extends Attribute {

    public static final String CustomShaderProviderAlias = "customShaderProvider";
    public static final long CustomShaderProvider = register(CustomShaderProviderAlias);

    public ShaderProvider shaderProvider;
    public Object userData;

    public CustomShaderProviderAttribute(ShaderProvider shaderProvider) {
        this(shaderProvider, null);
    }

    public CustomShaderProviderAttribute(ShaderProvider shaderProvider, Object userData) {
        super(CustomShaderProvider);
        this.shaderProvider = shaderProvider;
        this.userData = userData;
    }

    public <T> T getUserData(Class<T> type) {
        return (T) this.userData;
    }

    @Override
    public Attribute copy() {
        return new CustomShaderProviderAttribute(this.shaderProvider, this.userData);
    }

    @Override
    public int compareTo(Attribute o) {
        return 0;
    }
}
