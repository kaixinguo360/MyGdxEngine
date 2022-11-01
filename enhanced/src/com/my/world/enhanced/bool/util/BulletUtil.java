package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.utils.Array;

import java.util.Map;

public class BulletUtil {

    public static btConvexHullShape getConvexHullShape(ModelInstance instance) {
        return getConvexHullShape(instance.nodes);
    }

    public static btConvexHullShape getConvexHullShape(Model model) {
        return getConvexHullShape(model.nodes);
    }

    public static btConvexHullShape getConvexHullShape(Array<Node> nodes) {
        Map<Mesh, MeshGroup> meshes = MeshGroup.getMeshGroups(nodes);

        if (meshes.size() == 0) {
            return null;
        }

        btConvexHullShape shape = new btConvexHullShape();

        for (Map.Entry<Mesh, MeshGroup> entry : meshes.entrySet()) {

            float[] vertices = MeshUtil.getVertices(entry.getKey());
            short[] indices = MeshUtil.getIndices(entry.getKey());
            int vertexSize = entry.getKey().getVertexSize() / 4;
            int offsetPosition = MeshUtil.getPositionOffset(entry.getKey());

            for (MeshGroup.MeshNodePart nodePart : entry.getValue().meshNodeParts) {
                MeshPart meshPart = nodePart.meshPart;
                addVerticesToConvexHullShape(shape, nodePart.node.calculateLocalTransform(), indices, meshPart.offset, meshPart.size, vertices, vertexSize, offsetPosition);
            }
        }

        shape.initializePolyhedralFeatures();
        shape.recalcLocalAabb();
        shape.optimizeConvexHull();

        return shape;
    }

    private static void addVerticesToConvexHullShape(
            btConvexHullShape shape, Matrix4 transform,
            short[] indices, int indexOffset, int indexNum,
            float[] vertices, int vertexSize, int offsetPosition) {
        assert (indexOffset + indexNum <= indices.length);
        assert (offsetPosition + 3 <= vertexSize);

        Vector3 tmpV = new Vector3();
        for (int i = 0; i < indexNum; i++) {
            int vertexOffset = indices[indexOffset + i] * vertexSize;
            float x = vertices[vertexOffset + offsetPosition];
            float y = vertices[vertexOffset + offsetPosition + 1];
            float z = vertices[vertexOffset + offsetPosition + 2];
            shape.addPoint(tmpV.set(x, y, z).mul(transform), false);
        }
    }
}
