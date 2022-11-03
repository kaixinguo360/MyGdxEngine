package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.my.world.core.util.Disposable;
import com.my.world.enhanced.bool.util.EnhancedPool;
import lombok.var;

import java.util.Arrays;

public class VertexData implements Disposable {

    public float[] values;
    public VertexAttributes attributes;

    public static final EnhancedPool<VertexData> pool = new EnhancedPool<>(VertexData::new);

    public static VertexData obtain(float[] values, VertexAttributes attributes) {
        var obtain = pool.obtain();
        obtain.values = values;
        obtain.attributes = attributes;
        return obtain;
    }

    @Override
    public Object clone() {
        return VertexData.obtain(values.clone(), attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VertexData other = (VertexData) obj;
        if (!this.attributes.equals(other.attributes)) return false;
        return Arrays.equals(this.values, other.values);
    }

    @Override
    public void dispose() {
        this.values = null;
        this.attributes = null;
    }
}
