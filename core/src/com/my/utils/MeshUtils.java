package com.my.utils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;

import java.util.Map;

public class MeshUtils {

    public static float[] getVertices(Mesh mesh) {
        return mesh.getVertices(new float[
                mesh.getNumVertices() * mesh.getVertexSize()
                ]);
    }

    public static short[] getIndices(Mesh mesh) {
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        return indices;
    }

    public static int getPositionOffset(Mesh mesh) {
        int offsetPosition = 0; //获取顶点属性偏移 - 位置 - Mesh
        for(VertexAttribute attribute : mesh.getVertexAttributes()) {
            int offset = attribute.offset / 4;
            switch (attribute.usage) {
                case VertexAttributes.Usage.Position:
                    offsetPosition = offset;
                    break;
            }
        }
        return offsetPosition;
    }

    public static void mergeMeshPart(ModelInstance instance) {
        Map<Mesh, MeshGroup> meshGroups = MeshGroup.getMeshGroupsFromModelInstance(instance);
        Array<MeshGroup.MyNodePart> myNodeParts = new Array<>();

        for(Map.Entry<Mesh, MeshGroup> entry : meshGroups.entrySet()) {
            MeshGroup.MyNodePart firstNodePart = entry.getValue().myNodeParts.first();
            myNodeParts.add(firstNodePart);
        }

        instance.nodes.clear();

        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            Node node = myNodePart.node;
            node.detach();

            node.translation.setZero();
            node.scale.set(1, 1, 1);
            node.rotation.idt();
            node.isAnimated = false;
            node.calculateLocalTransform();

            node.parts.clear();
            node.parts.add(myNodePart.nodePart);

            MeshPart meshPart = myNodePart.meshPart;
            meshPart.set(meshPart.id, meshPart.mesh, 0, meshPart.mesh.getNumIndices(), meshPart.primitiveType);

            instance.nodes.add(node);
        }
    }

    public static boolean isOneMeshPart(ModelInstance instance) {
        Map<Mesh, MeshGroup> meshGroups = MeshGroup.getMeshGroupsFromModelInstance(instance);

        if(meshGroups.size() == 1) {
            for(MeshGroup meshGroup : meshGroups.values()) {
                if(meshGroup.myNodeParts.size != 1)
                    return false;
            }
            return true;
        } else
            return false;
    }

    public static boolean hasMesh(ModelInstance instance) {
        if(instance.nodes.size == 0) return false;
        Map<Mesh, MeshGroup> meshGroups = MeshGroup.getMeshGroupsFromModelInstance(instance);
        return (meshGroups.size() > 0);
    }
}
