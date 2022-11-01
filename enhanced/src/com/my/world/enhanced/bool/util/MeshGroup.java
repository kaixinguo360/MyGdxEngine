package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class MeshGroup {

    public final Mesh mesh;
    public final Array<MyNodePart> myNodeParts = new Array<>();

    public MeshGroup(Mesh mesh) {
        this.mesh = mesh;
    }

    public static MeshGroup getFirstMeshGroupFromModelInstance(ModelInstance modelInstance) {
        Map<Mesh, MeshGroup> meshGroups = getMeshGroupsFromModelInstance(modelInstance);
        MeshGroup meshGroup = null;
        for (MeshGroup m : meshGroups.values()) {
            meshGroup = m;
            break;
        }
        return meshGroup;
    }

    public static Map<Mesh, MeshGroup> getMeshGroupsFromModel(Model model) {
        Map<Mesh, MeshGroup> meshGroups = new HashMap<>();
        for (Node node : model.nodes) {
            addNode(node, null, meshGroups);
        }
        return meshGroups;
    }

    public static Map<Mesh, MeshGroup> getMeshGroupsFromModelInstance(ModelInstance modelInstance) {
        Map<Mesh, MeshGroup> meshGroups = new HashMap<>();
        for (Node node : modelInstance.nodes) {
            addNode(node, null, meshGroups);
        }
        return meshGroups;
    }

    //---------------------------------- Static --------------------------------------//

    private static void addNode(Node node, Node parentNode, Map<Mesh, MeshGroup> meshGroups) {
        for (NodePart nodePart : node.parts) {
            MeshPart meshPart = nodePart.meshPart;
            if (!meshGroups.containsKey(meshPart.mesh))
                meshGroups.put(meshPart.mesh, new MeshGroup(meshPart.mesh));
            MeshGroup meshGroup = meshGroups.get(meshPart.mesh);
            if (!meshGroup.contains(nodePart, true))
                meshGroup.add(meshGroup.new MyNodePart(nodePart, node, parentNode));
        }
        for (Node child : node.getChildren()) {
            addNode(child, node, meshGroups);
        }
    }

    public void add(MyNodePart myNodePart) {
        myNodeParts.add(myNodePart);
    }

    public boolean contains(NodePart value, boolean identity) {
        int i = myNodeParts.size - 1;
        if (identity || value == null) {
            while (i >= 0)
                if (myNodeParts.get(i--).nodePart == value) return true;
        } else {
            while (i >= 0)
                if (value.equals(myNodeParts.get(i--).nodePart)) return true;
        }
        return false;
    }

    public class MyNodePart {
        public final NodePart nodePart;
        public final MeshPart meshPart;
        public final Node node;
        public final Node parentNode;
        public final Mesh mesh;
        public Object userObject;

        public MyNodePart(NodePart nodePart, Node node, Node parentNode) {
            this.nodePart = nodePart;
            this.meshPart = nodePart.meshPart;
            this.node = node;
            this.parentNode = parentNode;
            this.mesh = MeshGroup.this.mesh;
        }
    }
}
