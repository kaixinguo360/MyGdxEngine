package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;
import com.my.world.core.util.Disposable;
import lombok.var;

public class VertexData implements Disposable {

    public float[] values;
    public Mesh mesh;

    public static final EnhancedPool<VertexData> pool = new EnhancedPool<>(VertexData::new);

    public static VertexData obtain(float[] values, Mesh mesh) {
        var obtain = pool.obtain();
        obtain.values = values;
        obtain.mesh = mesh;
        return obtain;
    }

    @Override
    public Object clone() {
        return VertexData.obtain(values.clone(), mesh);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VertexData other = (VertexData) obj;
        if (this.mesh != other.mesh) return false;
        return this.values == other.values; // TODO: 两个MyData怎么才算相等?
    }

    @Override
    public void dispose() {
        this.values = null;
        this.mesh = null;
    }
}
