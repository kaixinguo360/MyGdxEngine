package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.util.Disposable;
import com.my.world.enhanced.bool.util.LoggerUtil;
import com.my.world.enhanced.bool.util.NumberUtil;
import lombok.var;

import java.util.ArrayList;

import static com.badlogic.gdx.math.Matrix4.*;

/**
 * Represents of a 3d face vertex.
 *
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Vertex implements Cloneable, Disposable {

    /**
     * vertex status if it is still unknown
     */
    public static final int UNKNOWN = 1;
    /**
     * vertex status if it is inside a solid
     */
    public static final int INSIDE = 2;
    /**
     * vertex status if it is outside a solid
     */
    public static final int OUTSIDE = 3;
    /**
     * vertex status if it on the boundary of a solid
     */
    public static final int BOUNDARY = 4;

    /**
     * vertex coordinate in X
     */
    public double x;
    /**
     * vertex coordinate in Y
     */
    public double y;
    /**
     * vertex coordinate in Z
     */
    public double z;
    /**
     * references to vertices conected to it by an edge
     */
    private final ArrayList<Vertex> adjacentVertices = new ArrayList<>();
    /**
     * vertex status relative to other object
     */
    private int status;
    /**
     * vertex data
     */
    public VertexData data;

    //----------------------------------CONSTRUCTORS--------------------------------//

    public static final EnhancedPool<Vertex> pool = new EnhancedPool<>(Vertex::new);

    public static Vertex obtain(VectorD position, VertexData data, int status) {
        var obtain = pool.obtain();
        obtain.data = (VertexData) data.clone();
        obtain.x = position.x;
        obtain.y = position.y;
        obtain.z = position.z;
        obtain.status = status;
        return obtain;
    }

    //-----------------------------------OVERRIDES----------------------------------//

    /**
     * Clones the vertex object
     *
     * @return cloned vertex object
     */
    public Object clone() {
        Vertex clone = pool.obtain();
        clone.x = x;
        clone.y = y;
        clone.z = z;
        clone.data = (VertexData) data.clone();
        clone.status = status;
        for (int i = 0; i < adjacentVertices.size(); i++) {
            clone.adjacentVertices.add((Vertex) adjacentVertices.get(i).clone());
        }

        return clone;
    }

    /**
     * Makes a string definition for the Vertex object
     *
     * @return the string definition
     */
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Checks if an vertex is equal to another. To be equal, they have to have the same
     * coordinates(with some tolerance) and data
     *
     * @param anObject the other vertex to be tested
     * @return true if they are equal, false otherwise.
     */
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Vertex)) {
            return false;
        } else {
            Vertex vertex = (Vertex) anObject;
            return Math.abs(x - vertex.x) < NumberUtil.fTOL && Math.abs(y - vertex.y) < NumberUtil.fTOL
                    && Math.abs(z - vertex.z) < NumberUtil.fTOL && data.equals(vertex.data);
        }
    }

    //--------------------------------------SETS------------------------------------//

    /**
     * Gets the vertex position
     *
     * @return vertex position
     */
    public VectorD getPosition() {
        return VectorD.obtain().set(x, y, z);
    }

    //--------------------------------------GETS------------------------------------//

    /**
     * Gets an array with the adjacent vertices
     *
     * @return array of the adjacent vertices
     */
    public Vertex[] getAdjacentVertices() {
        Vertex[] vertices = new Vertex[adjacentVertices.size()];
        for (int i = 0; i < adjacentVertices.size(); i++) {
            vertices[i] = (Vertex) adjacentVertices.get(i);
        }
        return vertices;
    }

    /**
     * Gets the vertex status
     *
     * @return vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the vertex status
     *
     * @param status vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public void setStatus(int status) {
        if (status >= UNKNOWN && status <= BOUNDARY) {
            this.status = status;
        }
    }

    /**
     * Gets the vertex data
     *
     * @return vertex data
     */
    public VertexData getData() {
        return (VertexData) data.clone();
    }

    //----------------------------------OTHERS--------------------------------------//

    /**
     * Sets a vertex as being adjacent to it
     *
     * @param adjacentVertex an adjacent vertex
     */
    public void addAdjacentVertex(Vertex adjacentVertex) {
        if (!adjacentVertices.contains(adjacentVertex)) {
            adjacentVertices.add(adjacentVertex);
        }
    }

    /**
     * Sets the vertex status, setting equally the adjacent ones
     *
     * @param status new status to be set
     */
    public void mark(int status) {
        // mark vertex
        this.status = status;

        // mark adjacent vertices
        Vertex[] adjacentVerts = getAdjacentVertices();
        for (Vertex adjacentVert : adjacentVerts) {
            if (adjacentVert.getStatus() == Vertex.UNKNOWN) {
                adjacentVert.mark(status);
            }
        }
    }

    //----------------------------------MY--------------------------------------//

    public Vector3 toVector3(Vector3 vector3) {
        return vector3.set((float) x, (float) y, (float) z);
    }

    public Vertex set(Vector3 vector3) {
        x = vector3.x;
        y = vector3.y;
        z = vector3.z;
        return this;
    }

    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();

    public void avg(Vertex v1, Vertex v2, Vertex v3) {
        float[] data = this.data.values;
        float[] data1 = v1.data.values;
        float[] data2 = v2.data.values;
        float[] data3 = v3.data.values;
        if (data.length != data1.length || data1.length != data2.length || data2.length != data3.length) {
            LoggerUtil.log(1, "顶点大小不同, 跳过混合");
        }

        tmpM.idt();
        tmpM.val[M00] = (float) v1.x;
        tmpM.val[M10] = (float) v1.y;
        tmpM.val[M20] = (float) v1.z;
        tmpM.val[M01] = (float) v2.x;
        tmpM.val[M11] = (float) v2.y;
        tmpM.val[M21] = (float) v2.z;
        tmpM.val[M02] = (float) v3.x;
        tmpM.val[M12] = (float) v3.y;
        tmpM.val[M22] = (float) v3.z;
        try {
            tmpM.inv();
        } catch (Exception e) {
            LoggerUtil.log(1, "计算顶点权重出错");
            return;
        }
        this.toVector3(tmpV).mul(tmpM).nor();
        float scale = 1 / (tmpV.x + tmpV.y + tmpV.z);
        float p1 = tmpV.x * scale;
        float p2 = tmpV.y * scale;
        float p3 = tmpV.z * scale;

        for (int i = 0; i < data.length; i++) {
            data[i] = p1 * data1[i] + p2 * data2[i] + p3 * data3[i];
        }
    }

    @Override
    public void dispose() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.status = 0;
        this.adjacentVertices.clear();
        this.data = null;
    }
}
