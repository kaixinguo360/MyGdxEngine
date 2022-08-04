package com.my.world.enhanced.ragdoll;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.Vector3Pool;

public class MeshUtil {

    // 获取指定节点及其直接子节点的组合刚体形状
    public static btCompoundShape getCompoundShape(Node node, Matrix4 offset) {
        Matrix4 tmpM = Matrix4Pool.obtain();

        btCompoundShape shape = new btCompoundShape();
        boolean hasMesh = false;
        if (hasMesh(node)) {
            if (offset != null) tmpM.set(offset);
            shape.addChildShape(tmpM, getConvexHullShape(node, null));
            hasMesh = true;
        }
        for (Node subNode : node.getChildren()) {
            if (hasMesh(subNode)) {
                if (offset != null) tmpM.set(offset);
                tmpM.mul(subNode.calculateLocalTransform());
                shape.addChildShape(tmpM, getConvexHullShape(subNode, null));
                hasMesh = true;
                tmpM.inv();
            }
        }
        if (!hasMesh) throw new RuntimeException("No mesh found in this node: " + node.id);

        Matrix4Pool.free(tmpM);
        return shape;
    }

    // 获取指定节点的凸包刚体形状
    public static btConvexHullShape getConvexHullShape(Node node, Matrix4 offset) {

        btConvexHullShape shape = new btConvexHullShape();
        for (NodePart nodePart : node.parts) {
            MeshPart meshPart = nodePart.meshPart;
            Mesh mesh = meshPart.mesh;
            float[] vertices = getVertices(mesh);
            short[] indices = getIndices(mesh);
            int vertexSize = mesh.getVertexSize() / 4;
            int offsetPosition = getPositionOffset(mesh);
            addVerticesToConvexHullShape(shape, offset, indices, meshPart.offset, meshPart.size, vertices, vertexSize, offsetPosition);
        }

        shape.initializePolyhedralFeatures();
        shape.recalcLocalAabb();
        shape.optimizeConvexHull();

        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    // 判断节点是否包含网格
    public static boolean hasMesh(Node node) {
        return node.parts.size != 0;
    }

    // 获取网格的顶点数组
    public static float[] getVertices(Mesh mesh) {
        float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexSize()];
        mesh.getVertices(vertices);
        return vertices;
    }

    // 获取网格的索引数组
    public static short[] getIndices(Mesh mesh) {
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        return indices;
    }

    // 获取网格的顶点属性偏移
    public static int getPositionOffset(Mesh mesh) {
        for(VertexAttribute attribute : mesh.getVertexAttributes()) {
            int offset = attribute.offset / 4;
            if (attribute.usage == VertexAttributes.Usage.Position) {
                return offset;
            }
        }
        throw new RuntimeException("No position vertex attributes found in this mesh: " + mesh);
    }

    // 添加顶点到凸包刚体形状
    public static void addVerticesToConvexHullShape(
            btConvexHullShape shape,
            Matrix4 offset,
            short[] indices, int indexOffset, int indexNum,
            float[] vertices, int vertexSize, int offsetPosition
    ) {
        assert (indexOffset + indexNum <= indices.length);
        assert (offsetPosition + 3 <= vertexSize);

        Vector3 tmpV = Vector3Pool.obtain();
        for(int i = 0; i < indexNum; i++) {
            int vertexOffset = indices[indexOffset + i] * vertexSize;
            tmpV.set(
                    vertices[vertexOffset + offsetPosition],
                    vertices[vertexOffset + offsetPosition + 1],
                    vertices[vertexOffset + offsetPosition + 2]
            );
            if (offset != null) {
                tmpV.mul(offset);
            }
            shape.addPoint(tmpV, false);
        }
        Vector3Pool.free(tmpV);
    }
}
