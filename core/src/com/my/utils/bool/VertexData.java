package com.my.utils.bool;

import com.badlogic.gdx.graphics.Mesh;

public class VertexData {

    private float[] data = null;
    private Mesh mesh = null;

    public VertexData(float[] data, Mesh mesh) {
        this.data = data;
        this.mesh = mesh;
    }


    //---------------------------------- Getter --------------------------------------//

    public float[] getData() {
        return data;
    }

    public Mesh getMesh() {
        return mesh;
    }


    //---------------------------------- Setter --------------------------------------//

    public void setData(float[] data, Mesh mesh) {
        this.data = data;
        this.mesh = mesh;
    }


    //---------------------------------- Override --------------------------------------//

    @Override
    public Object clone() {
        float[] data = this.data.clone();
        return new VertexData(data, this.mesh);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VertexData other = (VertexData)obj;
        if (this.mesh != other.mesh) return false;
        if (this.data != other.data) return false; //TODO: 两个MyData怎么才算相等?
        return true;
    }
}
