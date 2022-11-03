package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;

public class VertexData {

    public float[] values;
    public Mesh mesh;

    public VertexData(float[] values, Mesh mesh) {
        this.values = values;
        this.mesh = mesh;
    }

    @Override
    public Object clone() {
        return new VertexData(values.clone(), mesh);
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
}
