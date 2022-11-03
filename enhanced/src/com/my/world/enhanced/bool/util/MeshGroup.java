package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;
import com.my.world.enhanced.bool.operation.ModelInstanceBoolOperation;

import java.util.HashMap;
import java.util.Map;

public class MeshGroup {

    public final Mesh mesh;
    public final Array<MeshNodePart> meshNodeParts = new Array<>();

    public MeshGroup(Mesh mesh) {
        this.mesh = mesh;
    }

    public void add(MeshNodePart meshNodePart) {
        meshNodeParts.add(meshNodePart);
    }

    public boolean contains(NodePart value, boolean identity) {
        int i = meshNodeParts.size - 1;
        if (identity || value == null) {
            while (i >= 0)
                if (meshNodeParts.get(i--).nodePart == value) return true;
        } else {
            while (i >= 0)
                if (value.equals(meshNodeParts.get(i--).nodePart)) return true;
        }
        return false;
    }

    private static void addNode(Node node, Map<Mesh, MeshGroup> meshGroups) {
        for (NodePart nodePart : node.parts) {
            MeshPart meshPart = nodePart.meshPart;
            if (!meshGroups.containsKey(meshPart.mesh))
                meshGroups.put(meshPart.mesh, new MeshGroup(meshPart.mesh));
            MeshGroup meshGroup = meshGroups.get(meshPart.mesh);
            if (!meshGroup.contains(nodePart, true))
                meshGroup.add(new MeshNodePart(nodePart, node));
        }
        for (Node child : node.getChildren()) {
            addNode(child, meshGroups);
        }
    }

    //---------------------------------- Static --------------------------------------//

    public static Map<Mesh, MeshGroup> getMeshGroups(Model model) {
        return getMeshGroups(model.nodes);
    }

    public static Map<Mesh, MeshGroup> getMeshGroups(ModelInstance modelInstance) {
        return getMeshGroups(modelInstance.nodes);
    }

    public static Map<Mesh, MeshGroup> getMeshGroups(Array<Node> nodes) {
        Map<Mesh, MeshGroup> meshGroups = new HashMap<>();
        for (Node node : nodes) {
            addNode(node, meshGroups);
        }
        return meshGroups;
    }

    //---------------------------------- Inner Class --------------------------------------//

    public static class MeshNodePart {
        public final NodePart nodePart;
        public final MeshPart meshPart;
        public final Node node;
        public ModelInstanceBoolOperation.BoolPair userObject;

        public MeshNodePart(NodePart nodePart, Node node) {
            this.nodePart = nodePart;
            this.meshPart = nodePart.meshPart;
            this.node = node;
        }
    }
}
