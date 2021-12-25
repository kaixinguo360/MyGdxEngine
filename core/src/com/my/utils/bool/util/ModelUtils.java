package com.my.utils.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.HashMap;
import java.util.Map;

public class ModelUtils {

    public static Array<Mesh> getMeshesFromModelInstance(ModelInstance modelInstance) {
        Array<Mesh> meshes = new Array<>();
        for(Node node : modelInstance.nodes) {
            addNode(node, meshes);
        }
        return meshes;
    }
    private static void addNode(Node node, Array<Mesh> meshes) {
        for(NodePart nodePart : node.parts) {
            MeshPart meshPart = nodePart.meshPart;
            if(!meshes.contains(meshPart.mesh, true))
                meshes.add(meshPart.mesh);
        }
        for(Node child : node.getChildren()) {
            addNode(child, meshes);
        }
    }

    public static Map<Mesh, Array<NodePart>> getMeshMapFromModelInstance(ModelInstance modelInstance) {
        Map<Mesh, Array<NodePart>> meshMap = new HashMap<>();
        for(Node node : modelInstance.nodes) {
            addNode(node, meshMap);
        }
        return meshMap;
    }
    private static void addNode(Node node, Map<Mesh, Array<NodePart>> meshMap) {
        for(NodePart nodePart : node.parts) {
            MeshPart meshPart = nodePart.meshPart;
            if(!meshMap.containsKey(meshPart.mesh))
                meshMap.put(meshPart.mesh, new Array<NodePart>());
            Array<NodePart> nodeParts = meshMap.get(meshPart.mesh);
            if(!nodeParts.contains(nodePart, true))
                nodeParts.add(nodePart);
        }
        for(Node child : node.getChildren()) {
            addNode(child, meshMap);
        }
    }

    //-------------------------------- Copy ----------------------------------//

    public static Model copyModel(final Model other) {

        Model model = new Model();

        for(Material material : other.materials) {
            model.materials.add(material.copy());
        }

        for(Animation animation : other.animations) {
            model.animations.add(animation);
        }

        for(Mesh mesh : other.meshes) {
            model.meshes.add(copyMesh(mesh));
            model.manageDisposable(mesh);
        }

        for(MeshPart meshPart : other.meshParts) {
            model.meshParts.add(copyMeshPart(meshPart, other, model));
        }

        for(Node node : other.nodes) {
            model.nodes.add(copyNode(node, other, model));
        }

        return model;
    }

    public static Node copyNode(final Node other, Model from,  Model target) {
        Node node = new Node();

        node.id = other.id;
        node.isAnimated = other.isAnimated;
        node.inheritTransform = other.inheritTransform;
        node.translation.set(other.translation);
        node.rotation.set(other.rotation);
        node.scale.set(other.scale);
        node.localTransform.set(other.localTransform);
        node.globalTransform.set(other.globalTransform);
        node.parts.clear();
        for (NodePart nodePart : other.parts) {
            node.parts.add(copyNodePart(nodePart, from, target));
        }
        for (Node child : other.getChildren()) {
            node.addChild(copyNode(child, from, target));
        }
        return node;
    }

    public static NodePart copyNodePart(final NodePart other, Model from,  Model target) {
        NodePart nodePart = new NodePart();

        if(from != null && target != null) {
            int index = from.meshParts.indexOf(other.meshPart, true);
            if(index != -1 && index < target.meshParts.size)
                nodePart.meshPart = target.meshParts.get(index);
            else {
                nodePart.meshPart = copyMeshPart(other.meshPart, from, target);
                target.meshParts.add(nodePart.meshPart);
            }
        } else {
            nodePart.meshPart = copyMeshPart(other.meshPart, from, target);
            if(target != null) target.meshParts.add(nodePart.meshPart);
        }

        nodePart.meshPart = copyMeshPart(other.meshPart, from, target);
        nodePart.material = other.material;
        nodePart.enabled = other.enabled;
        if (other.invBoneBindTransforms == null) {
            nodePart.invBoneBindTransforms = null;
            nodePart.bones = null;
        } else {
            if (nodePart.invBoneBindTransforms == null)
                nodePart.invBoneBindTransforms = new ArrayMap<Node, Matrix4>(true, other.invBoneBindTransforms.size, Node.class, Matrix4.class);
            else
                nodePart.invBoneBindTransforms.clear();
            nodePart.invBoneBindTransforms.putAll(other.invBoneBindTransforms);

            if (nodePart.bones == null || nodePart.bones.length != nodePart.invBoneBindTransforms.size)
                nodePart.bones = new Matrix4[nodePart.invBoneBindTransforms.size];

            for (int i = 0; i < nodePart.bones.length; i++) {
                if (nodePart.bones[i] == null)
                    nodePart.bones[i] = new Matrix4();
            }
        }
        return nodePart;
    }

    public static MeshPart copyMeshPart(final MeshPart other, Model from,  Model target) {
        MeshPart meshPart = new MeshPart();

        if(from != null && target != null) {
            int index = from.meshes.indexOf(other.mesh, true);
            if(index != -1 && index < target.meshes.size)
                meshPart.mesh = target.meshes.get(index);
            else {
                meshPart.mesh = copyMesh(other.mesh);
                target.meshes.add(meshPart.mesh);
            }
        } else {
            meshPart.mesh = copyMesh(other.mesh);
            if(target != null) target.meshes.add(meshPart.mesh);
        }

        meshPart.id = other.id;
        meshPart.offset = other.offset;
        meshPart.size = other.size;
        meshPart.primitiveType = other.primitiveType;
        meshPart.center.set(other.center);
        meshPart.halfExtents.set(other.halfExtents);
        meshPart.radius = other.radius;

        return meshPart;
    }

    public static Mesh copyMesh(final Mesh other) {
        return other.copy(false);
    }
}
