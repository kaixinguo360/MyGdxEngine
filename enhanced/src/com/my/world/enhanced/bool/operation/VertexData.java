package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;

public class VertexData {

    public float[] data;
    public Mesh mesh;

    public VertexData(float[] data, Mesh mesh) {
        this.data = data;
        this.mesh = mesh;
    }

    @Override
    public Object clone() {
        return new VertexData(data.clone(), mesh);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VertexData other = (VertexData) obj;
        if (this.mesh != other.mesh) return false;
        return this.data == other.data; // TODO: 两个MyData怎么才算相等?
    }
}
