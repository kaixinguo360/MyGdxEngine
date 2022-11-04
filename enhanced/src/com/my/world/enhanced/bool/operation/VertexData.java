package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.VertexAttributes;

public class VertexData {

    public float[] values;
    public VertexAttributes attributes;

    public VertexData(float[] values, VertexAttributes attributes) {
        this.values = values;
        this.attributes = attributes;
    }

    @Override
    public Object clone() {
        return new VertexData(values.clone(), attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VertexData other = (VertexData) obj;
        if (this.attributes != other.attributes) return false;
        return this.values == other.values; // TODO: 两个MyData怎么才算相等?
//        if (!this.attributes.equals(other.attributes)) return false;
//        return Arrays.equals(this.values, other.values);
    }
}
