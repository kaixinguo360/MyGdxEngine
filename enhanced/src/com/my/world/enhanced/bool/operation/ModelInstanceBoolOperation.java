package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.my.world.enhanced.bool.util.LoggerUtil;
import com.my.world.enhanced.bool.util.MeshGroup;
import com.my.world.enhanced.bool.util.VertexMixer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to apply boolean operations on ModelInstance.
 *
 * <br><br>Two 'Solid' objects are submitted to this class constructor. There is a methods for
 * each boolean operation. Each of these return a 'Solid' resulting from the application
 * of its operation into the submitted solids.
 *
 * <br><br>See: D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class ModelInstanceBoolOperation {
    public final static short UNKNOWN = 0;
    public final static short DIFF = 1;
    public final static short INTER = 2;
    public final static short UNION = 4;
    /**
     * 是否应该跳过
     **/
    public boolean skip = false;
    /**
     * 初始化attrUtils
     **/
    VertexMixer vertexMixer = new VertexMixer();
    /**
     * 待操作ModelInstance
     */
    private ModelInstance instance;
    /**
     * 待操作MeshGroups
     */
    private Map<Mesh, MeshGroup> meshGroups;
    /**
     * 待操作BoolNodeParts
     */
    private Array<MeshGroup.BoolNodePart> boolNodeParts = new Array<>();
    /**
     * 待操作Mesh序号
     */
    private final Map<Mesh, Integer> mapIds = new HashMap<>();
    /**
     * 操作类型
     **/
    private int type = UNKNOWN;
    /**
     * 待操作MeshPart计数
     **/
    private int count = 0;
    /**
     * 参考BoolNodePart
     */
    private final MeshPart referenceMeshPart; // TODO: 直接使用MeshGroup而不是MeshPart到来创建参考Solid

    //--------------------------------CONSTRUCTORS----------------------------------//

    /**
     * Constructs a MeshGroupBoolOperation object to apply boolean operation in two solids.
     * Makes preliminary calculations
     *
     * @param instance   ModelInstance where boolean operations will be applied
     * @param reference  refer MeshPart where boolean operations will be applied
     * @param transform2 transform of the second mesh
     */
    public ModelInstanceBoolOperation(ModelInstance instance,
                                      MeshPart reference,
                                      Matrix4 transform2) throws BooleanOperationException {
        LoggerUtil.log(0, "*** 开始创建ModelInstanceBoolUtil ***");

        // 保存ModelInstance
        this.instance = instance;

        // 获取ModelInstance的MeshGroup
        this.meshGroups = MeshGroup.getMeshGroups(instance);

        // 获取参考物体的NodePart
        referenceMeshPart = reference;
        LoggerUtil.log(0, "参考物体MeshPart: " + referenceMeshPart.id);

        // 获取参考物体变换m2
        Matrix4 m2 = transform2.cpy();

        // 创建参考物体object2
        Solid solid2Base = new Solid(referenceMeshPart, m2);
        Bound bound2 = solid2Base.getBound();

        // 开始遍历meshGroups
        int id = 0;
        for (MeshGroup meshGroup : meshGroups.values()) {
            LoggerUtil.log(0, "设置meshGroup" + 1);

            LoggerUtil.log(0, "设置输出属性...");
            addMesh(meshGroup.mesh);

            // representation to apply boolean operations
            LoggerUtil.log(0, "设置待操作meshPart...");
            for (MeshGroup.BoolNodePart boolNodePart : meshGroup.boolNodeParts) {
                boolNodeParts.add(boolNodePart);

                LoggerUtil.log(0, "处理meshPart: " + boolNodePart.meshPart.id);

                Matrix4 m1 = instance.transform.cpy().mul(boolNodePart.node.calculateLocalTransform());

                boolNodePart.meshPart.update();
                if (!isOverlap(boolNodePart.meshPart, m1, bound2)) {
                    LoggerUtil.log(0, " ->| 终止 (边界盒未覆盖)"); // TODO: 会错误的略过未相交的meshPart
                    continue;
                }

                LoggerUtil.log(0, " -> 正在创建Solid");
                Solid solid1 = new Solid(boolNodePart.meshPart, m1);
                Solid solid2 = (Solid) solid2Base.clone();

                // split the faces so that none of them intercepts each other
                LoggerUtil.log(0, " -> 正在分割面1");
                boolean isSplit1, isSplit2;
                isSplit1 = solid1.splitFaces(solid2);
                LoggerUtil.log(0, " -> 正在分割面2");
                isSplit2 = solid2.splitFaces(solid1);

                if (isSplit1 || isSplit2) {

                    // classify faces as being inside or outside the other solid

                    LoggerUtil.log(0, " -> 正在分类面1");
                    solid1.classifyFaces(solid2);

                    LoggerUtil.log(0, " -> 正在分类面2");
                    solid2.classifyFaces(solid1);

                    BoolPair pair = new BoolPair(solid1, solid2);
                    pair.m1 = m1.inv();
                    boolNodePart.userObject = pair;

                    count++;
                } else {
                    LoggerUtil.log(0, " ->| 终止 (没有相交面)");
                }
                LoggerUtil.log(0, "MeshPart[" + boolNodePart.meshPart.id + "] 处理完毕!");
            }
            LoggerUtil.log(0, "Mesh[" + meshGroup.mesh + "] 处理完毕!");
            id++;
        }
        LoggerUtil.log(1, "处理Mesh[" + id + "]个, MeshPart[" + count + "]个");

        // 设置AttrProvider
        setAttrProvider();
        addMesh(referenceMeshPart.mesh);

        if (count == 0) {
            skip = true;
        }

        LoggerUtil.log(0, "*** 输入设置完毕 ***");
    }

    private void addMesh(Mesh mesh) {
        mapIds.put(mesh, mapIds.size());
        vertexMixer.addAttributes(mesh.getVertexAttributes());
    }

    /**
     * Set AttrProvider of VertexMixer
     */
    private void setAttrProvider() {
        vertexMixer.setAttrProvider(new VertexMixer.AttrProvider() {
            @Override
            public void setAttr(VertexAttribute v, float x, float y, float z, float[] attrs) {
                switch (v.usage) {
                    case VertexAttributes.Usage.Position: {
                        setPosition(attrs, 0, 0, 0);
                        break;
                    }
                    case VertexAttributes.Usage.ColorUnpacked: {
                        setColorUnpacked(attrs, 1, 0, 0, 1);
                        break;
                    }
                    case VertexAttributes.Usage.ColorPacked: {
                        setColorPacked(attrs, 1, 0, 0, 1);
                        break;
                    }
                    case VertexAttributes.Usage.Normal: {
                        setNormal(attrs, x, y, z);
                        break;
                    }
                    case VertexAttributes.Usage.TextureCoordinates: {
                        setTextureCoordinates(attrs, (float) Math.random(), (float) Math.random());
                        break;
                    }
                    case VertexAttributes.Usage.Generic: {
                        // No Thing In Here
                        break;
                    }
                    case VertexAttributes.Usage.BoneWeight: {
                        setBoneWeight(attrs, 0, 1);
                        break;
                    }
                    case VertexAttributes.Usage.BiNormal: {
                        setBiNormal(attrs, 0, 1, 0);
                        break;
                    }
                }
            }
        });
    }

    //-------------------------------BOOLEAN_OPERATIONS-----------------------------//


    /**
     * Do union operation between the input MeshGroups, save the results to each BoolPair.
     */
    public boolean doUnion() {
        type = UNION;
        LoggerUtil.log(0, "运行doUnion...");

        if (count == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolPair pair = (BoolPair) boolNodePart.userObject;
            Solid solid1 = pair.solid1;
            Solid solid2 = pair.solid2;
            pair.verData = composeMesh(solid1, solid2, Face.OUTSIDE, Face.SAME, Face.OUTSIDE);
        }

        return true;
    }


    /**
     * Do intersection operation between the input MeshGroups, save the results to each BoolPair.
     */
    public boolean doIntersection() {
        type = INTER;
        LoggerUtil.log(0, "运行doIntersection...");

        if (count == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolPair pair = (BoolPair) boolNodePart.userObject;
            Solid solid1 = pair.solid1;
            Solid solid2 = pair.solid2;
            pair.verData = composeMesh(solid1, solid2, Face.INSIDE, Face.SAME, Face.INSIDE);
        }

        return true;
    }

    /**
     * Do difference operation between the input MeshGroups, save the results to each BoolPair.
     */
    public boolean doDifference() {
        type = DIFF;
        LoggerUtil.log(0, "运行doDifference...");

        if (count == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolPair pair = (BoolPair) boolNodePart.userObject;
            Solid solid1 = pair.solid1;
            Solid solid2 = pair.solid2;

            solid2.invertInsideFaces();
            pair.verData = composeMesh(solid1, solid2, Face.OUTSIDE, Face.OPPOSITE, Face.INSIDE);
            solid2.invertInsideFaces();
        }

        return true;
    }


    //-------------------------------OUTPUT-----------------------------//

    /**
     * Get the type of this boolean operation.
     *
     * @return The type of this boolean operation
     */
    public int getType() {
        return type;
    }

    /**
     * Apply the results of boolean operation to the input MeshGroup.
     */
    public void apply() {
        LoggerUtil.log(0, "*** 开始创建输出 ***");

        // 获取总顶点数, 总索引数, 目标顶点大小
        int allVerNum = 0;
        int allIndexNum = 0;
        int verSize = vertexMixer.getTargetVertexSize();
        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolVerData verData = ((BoolPair) boolNodePart.userObject).verData;
            // 如果verData为空
            if (verData == null) {
                continue;
            }
            allVerNum += verData.vertexNum;
            allIndexNum += verData.indexNum;
        }

        // 新建对应数组
        // TODO: 如果输出顶点数大于short类型最大值, 则划分成多个Mesh
        LoggerUtil.log(0, "总顶点数: " + allVerNum);
        LoggerUtil.log(0, "总索引数: " + allIndexNum);
        assert ((allVerNum) < Short.MAX_VALUE) : "Too Many Vertexes!: " + allVerNum;
        float[] vs = new float[verSize * allVerNum];
        short[] is = new short[allIndexNum];

        int vsOffset = 0;
        int isOffset = 0;
        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolPair pair = (BoolPair) boolNodePart.userObject;
            BoolVerData verData = pair.verData;

            // 如果verData为空
            if (verData == null) {
                continue;
            }

            verData.copyToArray(vs, vsOffset, is, isOffset, pair.m1);

            vsOffset += verData.vertexNum;
            isOffset += verData.indexNum;

            LoggerUtil.log(0, "meshPart[" + boolNodePart.meshPart.id + "]: 顶点: " + verData.vertexNum + ", 索引: " + verData.indexNum);
            LoggerUtil.log(0, "meshPart[" + boolNodePart.meshPart.id + "]: vsOffset: " + vsOffset + ", isOffset: " + isOffset);


//            boolNodePart.node.localTransform.set(pair.m1.inv());
//            boolNodePart.node.isAnimated = true;
        }

        assert (vsOffset == allVerNum) : "顶点数不符合!";
        assert (isOffset == allIndexNum) : "索引数不符合!";

        // 使用对应数组创建Mesh
        Mesh mesh = new Mesh(false, allVerNum, allIndexNum, vertexMixer.getTargetAttr());
        mesh.setVertices(vs);
        mesh.setIndices(is);

        // 令每一个MeshPart使用新Mesh
        int tmpOffset = 0;
        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts) {
            if (boolNodePart.userObject == null) continue;

            BoolVerData verData = ((BoolPair) boolNodePart.userObject).verData;

            // 如果verData为空
            if (verData == null) {
                LoggerUtil.log(0, "删除meshPart: " + boolNodePart.meshPart.id);
                boolNodePart.node.parts.removeValue(boolNodePart.nodePart, true);
            } else {
                LoggerUtil.log(0, "更新meshPart: " + boolNodePart.meshPart.id);
                MeshPart meshPart = boolNodePart.meshPart;
                LoggerUtil.log(0, "tmpOffset: " + tmpOffset);
                LoggerUtil.log(0, "indexNum: " + verData.indexNum);
                meshPart.set(meshPart.id, mesh, tmpOffset, verData.indexNum, meshPart.primitiveType);
                meshPart.update();
//            boolNodePart.node.localTransform.translate(meshPart.center.cpy().scl(-1));
//            meshPart.center.set(0, 0, 0);
                tmpOffset += verData.indexNum;
            }
        }

        // 完毕
        LoggerUtil.log(0, "*** 输出创建完毕 ***");
    }

    /**
     * Apply the results of boolean operation to the input MeshGroup.
     */
    public ModelInstance getNewModelInstance() {
        LoggerUtil.log(0, "*** 开始创建输出 ***");

        // 临时保存原变量
        ModelInstance instance_old = this.instance;
        Map<Mesh, MeshGroup> meshGroups_old = this.meshGroups;
        Array<MeshGroup.BoolNodePart> boolNodeParts_old = this.boolNodeParts;

        // 覆盖原变量
        this.instance = instance.copy();
        this.meshGroups = MeshGroup.getMeshGroups(instance);
        for (MeshGroup meshGroup : meshGroups_old.values()) {
            MeshGroup newMeshGroup = meshGroups.get(meshGroup.mesh);
            assert (newMeshGroup != null);
            boolean isCopied;
            for (MeshGroup.BoolNodePart boolNodePart : meshGroup.boolNodeParts) {
                isCopied = false;
                for (MeshGroup.BoolNodePart newBoolNodePart : newMeshGroup.boolNodeParts) {
                    if (!boolNodePart.meshPart.id.equals(newBoolNodePart.meshPart.id))
                        continue;
                    newBoolNodePart.userObject = boolNodePart.userObject;
                    isCopied = true;
                    break;
                }
                assert (isCopied);
            }
        }
        this.boolNodeParts = new Array<>();
        for (MeshGroup.BoolNodePart boolNodePart : boolNodeParts_old) {
            for (MeshGroup newMeshGroup : meshGroups.values()) {
                for (MeshGroup.BoolNodePart newBoolNodePart : newMeshGroup.boolNodeParts) {
                    if (!boolNodePart.meshPart.id.equals(newBoolNodePart.meshPart.id))
                        continue;
                    boolNodeParts.add(newBoolNodePart);
                    break;
                }
            }
        }
        assert (boolNodeParts.size == this.boolNodeParts.size);

        // 调用原apply函数
        apply();
        ModelInstance newInstance = instance;

        // 还原临时保存的原变量
        this.instance = instance_old;
        this.meshGroups = meshGroups_old;
        this.boolNodeParts = boolNodeParts_old;

        return newInstance;
    }

    //--------------------------PRIVATES--------------------------------------------//

    /**
     * Composes a Mesh based on the faces status of the two operators solids:
     * Face.INSIDE, Face.OUTSIDE, Face.SAME, Face.OPPOSITE
     *
     * @param faceStatus1 status expected for the first solid faces
     * @param faceStatus2 other status expected for the first solid faces
     *                    (expected a status for the faces coincident with second solid faces)
     * @param faceStatus3 status expected for the second solid faces
     */
    private BoolVerData composeMesh(Solid solid1, Solid solid2, int faceStatus1, int faceStatus2, int faceStatus3) {
        LoggerUtil.log(0, "运行composeMesh...");

        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<VertexData> datas = new ArrayList<>();

        // group the elements of the two solids whose faces fit with the desired status
        groupObjectComponents(solid1, vertices, indices, datas, faceStatus1, faceStatus2);
        groupObjectComponents(solid2, vertices, indices, datas, faceStatus3, faceStatus3);

        if (indices.size() == 0) {
            return null;
        }

        // returns the solid containing the grouped elements
        return new BoolVerData(vertices, indices, datas);
    }

    /**
     * Fills solid arrays with data about faces of an object generated whose status
     * is as required
     *
     * @param solid      solid object used to fill the arrays
     * @param vertices    vertices array to be filled
     * @param indices     indices array to be filled
     * @param datas       datas array to be filled
     * @param faceStatus1 a status expected for the faces used to to fill the data arrays
     * @param faceStatus2 a status expected for the faces used to to fill the data arrays
     */
    private void groupObjectComponents(Solid solid, ArrayList<Vertex> vertices, ArrayList<Integer> indices, ArrayList<VertexData> datas, int faceStatus1, int faceStatus2) {
        LoggerUtil.log(0, "运行groupObjectComponents...");

        Face face;
        // for each face..
        for (int i = 0; i < solid.getNumFaces(); i++) {
            face = solid.getFace(i);
            // if the face status fits with the desired status...
            if (face.getStatus() == faceStatus1 || face.getStatus() == faceStatus2) {
                // adds the face elements into the arrays
                Vertex[] faceVerts = {face.v1, face.v2, face.v3};
                for (Vertex faceVert : faceVerts) {
                    if (vertices.contains(faceVert)) {
                        indices.add(vertices.indexOf(faceVert));
                    } else {
                        indices.add(vertices.size());
                        vertices.add(faceVert);
                        datas.add(faceVert.getData());
                    }
                }
            }
        }
    }

    /**
     * Test the input MeshPart and Bound is overlapped or not
     *
     * @param meshPart  input MeshPart
     * @param transform transform of the input MeshPart
     * @param bound     input Bound
     * @return is overlapped or not
     */
    private boolean isOverlap(MeshPart meshPart, Matrix4 transform, Bound bound) {
        meshPart.update();
        float radius = meshPart.radius;
        Vector3 center = new Vector3(meshPart.center).mul(transform);

        if (center.x > bound.xMax + radius || center.x < bound.xMin - radius) return false;
        if (center.y > bound.yMax + radius || center.y < bound.yMin - radius) return false;
        return !(center.z > bound.zMax + radius) && !(center.z < bound.zMin - radius);
    }

    //-------------------------- Inner Class --------------------------------------------//

    /**
     * Class to store the operation data in each boolean operation pairs.
     */
    private class BoolPair {
        private final Solid solid1, solid2;
        private Matrix4 m1, m2;
        private BoolVerData verData;

        private BoolPair(Solid solid1, Solid solid2) {
            this.solid1 = solid1;
            this.solid2 = solid2;
        }
    }

    /**
     * Class to store the vertex data and index data.
     */
    private class BoolVerData {
        private final ArrayList<Vertex> vertices;
        private final ArrayList<Integer> indexs;
        private final ArrayList<VertexData> datas;
        private final int vertexNum;
        private final int indexNum;

        private BoolVerData(ArrayList<Vertex> vertices, ArrayList<Integer> indexs, ArrayList<VertexData> datas) {
            assert (vertices.size() == datas.size()) : "vertices.vertexNum() != datas.vertexNum()";
            assert (vertices.size() < Short.MAX_VALUE) : "Too Many Vertices!"; // TODO: 解决输出顶点太多, 超出short类型最大值问题

            this.vertexNum = vertices.size();
            this.indexNum = indexs.size();

            this.vertices = vertices;
            this.indexs = indexs;
            this.datas = datas;
        }

        private void copyToArray(float[] vs, int vsOffset, short[] is, int isOffset, Matrix4 transform) {
            // 复制顶点数据
            Vector3 tmpV = new Vector3();
            vertexMixer.begin(vertexNum);
            for (int i = 0; i < vertexNum; i++) {
                vertices.get(i).toVector3(tmpV);
                tmpV.mul(transform);
                VertexData data = datas.get(i);

                Mesh mesh = data.mesh;
                if (mesh != null && mapIds.containsKey(data.mesh)) {
                    int id = mapIds.get(data.mesh);
                    if (id >= 0)
                        vertexMixer.addVertex(id, data.data, tmpV);
                    else
                        vertexMixer.addVertex(tmpV);
                } else {
                    vertexMixer.addVertex(tmpV);
                }
            }
            vertexMixer.buildToArray(vs, vsOffset);

            // 复制索引数据
            int max = 0,
                    min = Integer.MAX_VALUE;
            for (int i = 0; i < indexs.size(); i++) {
                if (indexs.get(i) + vsOffset > Short.MAX_VALUE) {
                    throw new RuntimeException("Index Is Too Bigger!");
                }
                if (indexs.get(i) < min) min = indexs.get(i);
                if (indexs.get(i) > max) max = indexs.get(i);
                is[isOffset + i] = (short) (indexs.get(i) + vsOffset);
            }
            LoggerUtil.log(0, "MIN: " + (min + vsOffset) + ", MAX: " + (max + vsOffset));
        }
    }

}















