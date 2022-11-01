package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Map;

public class BoneUtil {

    public static void applyBoneTransform(ModelInstance instance) {
        applyBoneTransform(instance, true);
    }

    public static void applyBoneTransform(ModelInstance instance, boolean mergeMeshPart) {
        applyBoneTransform(instance, true, null);
    }

    public static void applyBoneTransform(ModelInstance instance, boolean mergeMeshPart, VertexAttributes attributes) {
        VertexMixer vertexMixer = null;
        if (attributes != null && attributes.size() != 0) {
            vertexMixer = new VertexMixer();
            vertexMixer.addAttributes(attributes);
            vertexMixer.disallowChange();
        }

        Map<Mesh, MeshGroup> meshes = MeshGroup.getMeshGroups(instance);

        for (Map.Entry<Mesh, MeshGroup> entry : meshes.entrySet()) {
            Vector3 tmpV = new Vector3(); // 临时变量
            Matrix4 tmpM = new Matrix4();
            Vector3 pos0 = new Vector3();

            Mesh mesh = entry.getKey(); // 初始化Mesh
            int vertexSize = mesh.getVertexSize() / 4; // 顶点大小 - Mesh
            int numVertices = mesh.getNumVertices(); // 顶点数量 - Mesh

            float[] vertices = new float[vertexSize * numVertices]; // 读取全部顶点数组 - Mesh
            mesh.getVertices(vertices);

            boolean[] usedIndices = new boolean[numVertices]; // 使用过的索引数组 - Mesh

            int offsetPosition = 0; // 获取顶点属性偏移 - 位置 - Mesh
            Array<Integer> offsetBoneWeights = new Array<>(); // 获取顶点属性偏移 - 骨骼权重 - Mesh
            for (VertexAttribute attribute : mesh.getVertexAttributes()) {
                int offset = attribute.offset / 4;
                switch (attribute.usage) {
                    case VertexAttributes.Usage.Position:
                        offsetPosition = offset;
                        break;
                    case VertexAttributes.Usage.BoneWeight:
                        offsetBoneWeights.add(offset);
                        break;
                }
            }

            MeshGroup NodeParts = entry.getValue(); // 初始化NodeParts数组
            for (MeshGroup.BoolNodePart boolNodePart : entry.getValue().boolNodeParts) { // 遍历NodeParts数组
                NodePart nodePart = boolNodePart.nodePart;
                MeshPart meshPart = nodePart.meshPart;

                short[] indices = new short[meshPart.size]; // 获取索引数组 - MeshPart
                mesh.getIndices(meshPart.offset, meshPart.size, indices, 0);

                boolean[] tmpIndices = new boolean[numVertices]; // 使用过的索引数组 - MeshPart
                for (int i = 0; i < meshPart.size; i++) {
                    tmpIndices[indices[i]] = !usedIndices[indices[i]]; // 在tmpIndices中标记此MeshPart中出现过的索引
                }

                for (int i = 0; i < tmpIndices.length; i++) {
                    if (!tmpIndices[i]) // 在tmpIndices中没有标记过的索引不参与计算
                        continue;
                    int offsetVertex = i * vertexSize,
                            x = offsetVertex + offsetPosition,
                            y = offsetVertex + offsetPosition + 1,
                            z = offsetVertex + offsetPosition + 2;
                    pos0.set(vertices[x], vertices[y], vertices[z]);

                    vertices[x] = 0;
                    vertices[y] = 0;
                    vertices[z] = 0;

                    float weight0 = 1;
                    for (int j = 0; j < offsetBoneWeights.size; j++) {
                        int offsetBoneWeight = offsetBoneWeights.get(j);
                        int index = (int) vertices[offsetVertex + offsetBoneWeight];
                        float weight = vertices[offsetVertex + offsetBoneWeight + 1];
                        if (weight == 0) continue;
                        weight0 -= weight;
                        if (index >= nodePart.bones.length) continue;
                        Matrix4 transform = nodePart.bones[index];

//                    System.out.print("[" + j + ":" + nodePart.invBoneBindTransforms.keys[index].id + "]: ");
//                    System.out.print("I=[" + index + "] ");
//                    System.out.print("W=[" + weight + "] ");
//                    transform.getTranslation(tmpV);
//                    System.out.print("Tran=" + tmpV + " ");
//                    transform.getScale(tmpV);
//                    System.out.print("Scale=" + tmpV + " ");
//                    System.out.print("Rot=" + transform.getRotation(new Quaternion()) + " ");
//                    System.out.print("Hash=" + transform.hashCode() + " ");

                        tmpV.set(pos0);
//                    System.out.print("\n Input=(" +
//                            tmpV.x + ", " +
//                            tmpV.y + ", " +
//                            tmpV.z + ") ");
                        if (!mergeMeshPart) {
                            tmpM.set(boolNodePart.node.localTransform);
                            tmpV.mul(tmpM.inv());
                        }
                        tmpV.mul(transform);
                        tmpV.scl(weight);

                        vertices[x] += tmpV.x;
                        vertices[y] += tmpV.y;
                        vertices[z] += tmpV.z;

//                    System.out.print("\n Result=(" +
//                            tmpV.x + ", " +
//                            tmpV.y + ", " +
//                            tmpV.z + ") ");
//                    System.out.print("\n After=(" +
//                            vertices[x] + ", " +
//                            vertices[y] + ", " +
//                            vertices[z] + ") ");
//                    System.out.print("\n");
                    }

                    if (false) { // 权重相加不为1时
//                    System.out.print("[Weight0]: ");
//                    System.out.print("W=[" + weight0 + "] ");
                        tmpV.set(pos0);
//                    System.out.print("\n Input=(" +
//                            tmpV.x + ", " +
//                            tmpV.y + ", " +
//                            tmpV.z + ") ");
                        tmpV.scl(weight0);
                        vertices[x] += tmpV.x;
                        vertices[y] += tmpV.y;
                        vertices[z] += tmpV.z;

//                    System.out.print("\n Result=(" +
//                            tmpV.x + ", " +
//                            tmpV.y + ", " +
//                            tmpV.z + ") ");
//                    System.out.print("\n After=(" +
//                            vertices[x] + ", " +
//                            vertices[y] + ", " +
//                            vertices[z] + ") ");
//                    System.out.print("\n");
                    }
//                System.out.print("[Out]: " + pos0);
//                System.out.print(" -> (" +
//                        vertices[x] + "," +
//                        vertices[y] + "," +
//                        vertices[z] + ") \n\n");
                }

                for (int i = 0; i < meshPart.size; i++) {
                    usedIndices[indices[i]] = tmpIndices[indices[i]] || usedIndices[indices[i]];
                }
            }
//            mesh.setVertices(vertices);

            // 设置新Mesh的顶点
            Mesh newMesh;
            if (vertexMixer != null) {
                int meshId = vertexMixer.addAttributes(mesh.getVertexAttributes()); // 添加属性到vertexMixer
                float[] tmpF = new float[vertexSize]; // 创建数组临时储存单个顶点数据
                vertexMixer.begin(numVertices);
                for (int i = 0; i < numVertices; i++) {
                    int offsetVertex = i * vertexSize;
                    System.arraycopy(vertices, offsetVertex, tmpF, 0, vertexSize);
                    vertexMixer.addVertex(meshId, tmpF);  // 添加顶点到VertexMixer
                }
                newMesh = new Mesh(false, mesh.getNumVertices(), mesh.getNumIndices(), vertexMixer.getTargetAttr());
                newMesh.setVertices(vertexMixer.build());
            } else {
                newMesh = new Mesh(false, mesh.getNumVertices(), mesh.getNumIndices(), mesh.getVertexAttributes());
                newMesh.setVertices(vertices);
            }

            // 设置新Mesh的索引
            short[] tmpS = new short[mesh.getNumIndices()];
            mesh.getIndices(tmpS);
            newMesh.setIndices(tmpS);

            // 应用新Mesh到每个MeshPart
            for (MeshGroup.BoolNodePart boolNodePart : entry.getValue().boolNodeParts) { // 遍历NodeParts数组
                NodePart nodePart = boolNodePart.nodePart;
                MeshPart meshPart = nodePart.meshPart;
                meshPart.set(meshPart.id, newMesh, meshPart.offset, meshPart.size, meshPart.primitiveType);
                meshPart.update();
            }
        }

        if (mergeMeshPart) {
            MeshUtil.mergeMeshPart(instance);
        }

        clearBones(instance);
    }

    public static void clearBones(ModelInstance instance) {
        for (Node node : instance.nodes) {
            clearBones(node);
        }
        instance.calculateTransforms();
    }

    private static void clearBones(Node node) {
        for (NodePart nodePart : node.parts) {
            nodePart.invBoneBindTransforms = null;
            nodePart.bones = null;
        }

        for (Node child : node.getChildren()) {
            clearBones(child);
        }
    }
}
