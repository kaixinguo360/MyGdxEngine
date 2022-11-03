package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.my.world.enhanced.bool.operation.VertexData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeshSplitter {

    // 原始Mesh
    private final Mesh mesh;
    // 原始顶点数组
    private final float[] verticesOrigin;

    // 面数组
    private final Array<Face> faces = new Array<>();
    // Face计数
    private int faceNum = 0;
    // 非重复顶点数组
    private final Array<Vertex> vertices = new Array<>();

    // 分类后的面数组
    private final Array<Array<Face>> parts = new Array<>();
    // 是否已经split
    private boolean hasSplit = false;


    //----------------构造函数----------------//
    public MeshSplitter(Mesh mesh) {
        // 保存原始Mesh
        this.mesh = mesh;

        // 临时变量
        Vector3 pos = new Vector3();
        Vertex v1, v2, v3, vertex;

        // 获取顶点位置偏移
        int offsetPosition = MeshUtil.getPositionOffset(mesh);

        // 获取原始顶点数组
        int vertexSize = mesh.getVertexSize() / 4; // 顶点大小
        int numVertices = mesh.getNumVertices(); // 顶点数量
        float[] vers = new float[vertexSize * numVertices]; // 读取全部顶点数组
        verticesOrigin = vers;
        mesh.getVertices(vers);
        Vector3[] verticesPoints = new Vector3[numVertices];
        Array<Vertex> verticesTmp = new Array<>();
        for (int i = 0; i < numVertices; i++) {
            int offsetVertex = i * vertexSize,
                    x = offsetVertex + offsetPosition,
                    y = offsetVertex + offsetPosition + 1,
                    z = offsetVertex + offsetPosition + 2;

            // 获取顶点xyz坐标
            pos.set(vers[x], vers[y], vers[z]);

            // 复制顶点数据到MyData对象
            float[] dataArray = new float[vertexSize];
            System.arraycopy(vers, offsetVertex, dataArray, 0, vertexSize);
            VertexData data = VertexData.obtain(dataArray, mesh.getVertexAttributes());

            verticesPoints[i] = pos;
            vertex = addVertex(pos, data);
            verticesTmp.add(vertex);
        }
        // 原始顶点数组获取完毕

        // 获取全部索引数组
        short[] indicesFromMesh = new short[mesh.getNumIndices()]; // 读取全部索引数组
        mesh.getIndices(indicesFromMesh);
        int[] indices = new int[indicesFromMesh.length];
        for (int i = 0; i < indicesFromMesh.length; i++) {
            indices[i] = indicesFromMesh[i];
        }
        // 全部索引数组获取完毕

        // create faces
        for (int i = 0; i < indices.length; i = i + 3) {
            v1 = verticesTmp.get(indices[i]);
            v2 = verticesTmp.get(indices[i + 1]);
            v3 = verticesTmp.get(indices[i + 2]);
            addFace(new Face(v1, v2, v3, (short) indices[i], (short) indices[i + 1], (short) indices[i + 2]));
        }
        assert (faceNum * 3 == mesh.getNumIndices());
    }

    // Split Input ModelInstance To Disconnected Parts
    public static List<ModelInstance> splitModeInstances(ModelInstance instance) {
        List<ModelInstance> out = new ArrayList<>();
        if (MeshUtil.isOneMeshPart(instance)) {  // TODO: 只能处理只有一个MeshPart的ModelInstance
            MeshSplitter separator = new MeshSplitter(instance.nodes.first().parts.first().meshPart.mesh);
            if (separator.split()) {
                Mesh mesh = separator.getMesh();
                int[][] indices = separator.getIndices();
                int i = 0;
                for (int[] indexes : indices) {
                    int offset = indexes[0];
                    int size = indexes[1];

//                    ModelInstance newInstance;
//                    if(i == indices.length - 1) {
//                        newInstance = instance;  // 最后一个使用原ModelInstance
//                    } else {
//                        newInstance = instance.copy();  // 其他使用复制的ModelInstance
//                    }
                    ModelInstance newInstance = instance.copy();
                    MeshPart meshPart = newInstance.nodes.first().parts.first().meshPart;
                    meshPart.set(
                            meshPart.id, mesh, offset, size, meshPart.primitiveType
                    );
                    out.add(newInstance);
                    i++;
                }
            } else {
                out.add(instance);
            }
        } else {
            out.add(instance);
        }
        return out;
    }

    // 添加面
    private void addFace(Face face) {
        faces.add(face);
        faceNum++;
        Vertex v1 = checkVertex(face.v1);
        Vertex v2 = checkVertex(face.v2);
        Vertex v3 = checkVertex(face.v3);
        connectVertices(v1, v2);
        connectVertices(v1, v3);
        connectVertices(v2, v3);
    }

    // 检查重复顶点, 若重复则返回已储存在数组里的那个  // TODO: 优化顶点数量
    private Vertex checkVertex(Vertex v) {
        int index = vertices.indexOf(v, false);
        if (index == -1) {
            return v;
        } else {
            return vertices.get(index);
        }
    }

    // 链接两个顶点, 默认输入的两个顶点都是储存在数组里的
    private void connectVertices(Vertex v1, Vertex v2) {
//        assert (vertices.contains(v1, false));
//        assert (vertices.contains(v2, false));
        v1.cons.add(v2);
        v2.cons.add(v1);
    }

    // 添加顶点, 自动抛弃重复顶点
    private Vertex addVertex(Vector3 pos, VertexData vd) {
        Vertex vertex = new Vertex(pos, vd);
        if (!vertices.contains(vertex, false))
            vertices.add(vertex);
        return vertex;
    }

    //----------------划分顶点----------------//
    private void splitVertices() {
        boolean isDone = false;
        int tag = 0;
        while (!isDone) {
            boolean isFound = false;
            Vertex vertex = null;
            for (Vertex v : vertices) {
                if (v.tag == -1) {
                    vertex = v;
                    isFound = true;
                    break;
                }
            }
            if (isFound) {
                if (traverseVertices(vertex, tag)) {
                    parts.add(new Array<Face>());
                    tag++;
                } else {
                    removeVertex(vertex);
                }
            } else {
                isDone = true;
            }
        }
        assert (parts.size == tag);
        LoggerUtil.log(0, "Vertices:" + vertices.size);
        LoggerUtil.log(0, "Face:" + faces.size);
        LoggerUtil.log(0, "PartsNum:" + tag);
    }

    // 遍历相连的顶点并标记
    private boolean traverseVertices(Vertex v, int tag) {
        if (v.tag != -1) {
            return true;
        }
        v.tag = tag;

        if (v.cons.size == 0) {
            return false;
        }

        for (Vertex vertex : v.cons) {
            traverseVertices(vertex, tag);
        }

        return true;
    }

    // 删除无用的顶点(默认输入的是没有链接别的顶点的)
    private void removeVertex(Vertex v) {
        LoggerUtil.log(4, "!!!");
        vertices.removeValue(v, true);
    }

    //----------------划分面----------------//
    private void splitFaces() {
        Iterator<Face> it = faces.iterator();
        while (it.hasNext()) {
            Face face = it.next();
            int tag = checkVertex(face.v1).tag;
            assert (tag < parts.size);
            assert (tag != -1);
            it.remove();
            parts.get(tag).add(face);
        }
        assert (faces.size == 0);
    }

    //----------------划分----------------//
    public boolean split() {
        splitVertices();
        if (parts.size == 1) return false;
        splitFaces();
        hasSplit = true;
        return true;
    }

    //----------------输出----------------//
    // 获取新的Mesh
    public Mesh getMesh() {
        assert (hasSplit) : "Not Split!";

        short[] indices = new short[faceNum * 3];
        int i = 0;
        for (Array<Face> faces : parts) {
            assert (faces.size > 0);
            for (Face face : faces) {
                int index = i * 3;
                indices[index] = face.i1;
                indices[index + 1] = face.i2;
                indices[index + 2] = face.i3;
                i++;
            }
        }
        assert (i == faceNum);

        Mesh newMesh = new Mesh(false, mesh.getNumVertices(), mesh.getNumIndices(), mesh.getVertexAttributes());
        assert (verticesOrigin.length == mesh.getNumVertices() * mesh.getVertexSize() / 4);
        assert (indices.length == mesh.getNumIndices());
        newMesh.setVertices(verticesOrigin);
        newMesh.setIndices(indices);

        return newMesh;
    }

    ////////////////////////////////

    // 获取新Mesh的Indices数组, 用于创建MeshPart
    public int[][] getIndices() {
        assert (hasSplit) : "Not Split!";

        int[][] indices = new int[parts.size][2];
        int i = 0;
        int offset = 0;
        for (Array<Face> faces : parts) {
            int size = faces.size * 3;
            indices[i][0] = offset;
            indices[i][1] = size;
            offset += size;
            i++;
        }
        assert (offset == faceNum * 3);

        return indices;
    }

    private static class Face {
        private final Vertex v1;
        private final Vertex v2;
        private final Vertex v3;
        private final short i1;
        private final short i2;
        private final short i3;

        public Face(Vertex v1, Vertex v2, Vertex v3, short i1, short i2, short i3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
        }
    }

    ////////////////////////////////

    private class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private final VertexData data;
        private final Array<Vertex> cons = new Array<>();
        private int tag = -1;
//        private static final float TOLL = 0.01f;

        public Vertex(Vector3 position, VertexData data) {
            this.data = (VertexData) data.clone();
            x = position.x;
            y = position.y;
            z = position.z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vertex) {
                Vertex other = (Vertex) obj;
                return (Math.abs(other.x - this.x) < NumberUtil.fTOL) &&
                        (Math.abs(other.y - this.y) < NumberUtil.fTOL) &&
                        (Math.abs(other.z - this.z) < NumberUtil.fTOL);
            }
            return false;
        }
    }
}
