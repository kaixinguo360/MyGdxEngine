package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.bool.util.LoggerUtil;
import com.my.world.enhanced.bool.util.MeshGroup;
import com.my.world.enhanced.bool.util.VertexMixer;

import java.util.*;

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

    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();

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
     * 待操作MeshNodeParts
     */
    private List<MeshGroup.MeshNodePart> meshNodeParts = new ArrayList<>();
    /**
     * 待操作Mesh序号
     */
    private final Map<Mesh, Integer> mapIds = new HashMap<>();
    /**
     * 操作类型
     **/
    private int type = UNKNOWN;

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
        LoggerUtil.log(0, "*** 创建布尔操作开始 ***");

        // Setup Reference Object
        LoggerUtil.log(0, "参考物体: " + reference.id);
        Matrix4 referenceTransform = tmpM.set(transform2);
        Solid referenceSolid = new Solid(reference, referenceTransform);
        Bound referenceBound = referenceSolid.getBound();

        // Setup Target Object
        LoggerUtil.log(0, "目标物体: " + instance);
        this.instance = instance;
        Collection<MeshGroup> meshGroups = MeshGroup.getMeshGroups(instance).values();
        LoggerUtil.log(0, "开始处理目标物体");
        for (MeshGroup meshGroup : meshGroups) {
            LoggerUtil.log(0, "  开始处理 Mesh(" + meshGroup.mesh + ")");

            addMesh(meshGroup.mesh);

            for (MeshGroup.MeshNodePart meshNodePart : meshGroup.meshNodeParts) {
                LoggerUtil.log(0, "    开始处理 MeshPart(" + meshNodePart.meshPart.id + ")");

                Matrix4 meshNodePartTransform = instance.transform.cpy().mul(meshNodePart.node.calculateLocalTransform());
                meshNodePart.meshPart.update();
                if (!isOverlap(meshNodePart.meshPart, meshNodePartTransform, referenceBound)) {
                    LoggerUtil.log(0, "    边界盒未相交, 终止"); // TODO: 会错误的略过未相交的meshPart
                    continue;
                }

                LoggerUtil.log(0, "      正在创建Solid...");
                Solid solid1 = new Solid(meshNodePart.meshPart, meshNodePartTransform);
                Solid solid2 = (Solid) referenceSolid.clone();

                // split the faces so that none of them intercepts each other
                LoggerUtil.log(0, "      正在分割目标网格...");
                boolean isSplit1, isSplit2;
                isSplit1 = solid1.splitFaces(solid2);
                LoggerUtil.log(0, "      正在分割参考网格...");
                isSplit2 = solid2.splitFaces(solid1);

                if (!isSplit1 && !isSplit2) {
                    LoggerUtil.log(0, "    无相交部分, 终止");
                    continue;
                }
                // classify faces as being inside or outside the other solid

                LoggerUtil.log(0, "      正在分类目标网格...");
                solid1.classifyFaces(solid2);

                LoggerUtil.log(0, "      正在分类参考网格...");
                solid2.classifyFaces(solid1);

                BoolPair pair = new BoolPair(solid1, solid2);
                pair.m1 = meshNodePartTransform.inv();
                meshNodePart.userObject = pair;
                meshNodeParts.add(meshNodePart);

                LoggerUtil.log(0, "    处理完成 MeshPart(" + meshNodePart.meshPart.id + ")");
            }
            LoggerUtil.log(0, "  处理完成 Mesh(" + meshGroup.mesh + ")");
        }
        LoggerUtil.log(1, "处理目标物体完成, MeshPart " + meshNodeParts.size() + " 个");

        // 设置AttrProvider
        setAttrProvider();
        addMesh(reference.mesh);

        if (meshNodeParts.size() == 0) {
            skip = true;
        }

        LoggerUtil.log(0, "*** 创建布尔操作结束 ***");
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
        LoggerUtil.log(0, "提取并集网格...");

        if (meshNodeParts.size() == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;

            BoolPair pair = meshNodePart.userObject;
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
        LoggerUtil.log(0, "提取交集网格...");

        if (meshNodeParts.size() == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;

            BoolPair pair = meshNodePart.userObject;
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
        LoggerUtil.log(0, "提取非集网格...");

        if (meshNodeParts.size() == 0) {
            LoggerUtil.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;

            BoolPair pair = meshNodePart.userObject;
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
        LoggerUtil.log(0, "*** 输出创建开始 ***");

        // 获取总顶点数, 总索引数, 目标顶点大小
        int allVerNum = 0;
        int allIndexNum = 0;
        int verSize = vertexMixer.getTargetVertexSize();
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;

            BoolVerData verData = meshNodePart.userObject.verData;
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
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;

            BoolPair pair = meshNodePart.userObject;
            BoolVerData verData = pair.verData;

            // 如果verData为空
            if (verData == null) {
                continue;
            }

            verData.copyToArray(vs, vsOffset, is, isOffset, pair.m1);

            vsOffset += verData.vertexNum;
            isOffset += verData.indexNum;

            LoggerUtil.log(0, "meshPart[" + meshNodePart.meshPart.id + "]: 顶点: " + verData.vertexNum + ", 索引: " + verData.indexNum);
            LoggerUtil.log(0, "meshPart[" + meshNodePart.meshPart.id + "]: vsOffset: " + vsOffset + ", isOffset: " + isOffset);


//            meshNodePart.node.localTransform.set(pair.m1.inv());
//            meshNodePart.node.isAnimated = true;
        }

        assert (vsOffset == allVerNum) : "顶点数不符合!";
        assert (isOffset == allIndexNum) : "索引数不符合!";

        // 使用对应数组创建Mesh
        Mesh mesh = new Mesh(false, allVerNum, allIndexNum, vertexMixer.getTargetAttr());
        mesh.setVertices(vs);
        mesh.setIndices(is);

        // 令每一个MeshPart使用新Mesh
        int offset = 0;
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;
            BoolVerData verData = meshNodePart.userObject.verData;

            // 如果verData为空
            if (verData == null) {
                meshNodePart.node.parts.removeValue(meshNodePart.nodePart, true);
            } else {
                MeshPart meshPart = meshNodePart.meshPart;
                meshPart.set(meshPart.id, mesh, offset, verData.indexNum, meshPart.primitiveType);
                meshPart.update();
                offset += verData.indexNum;
            }
        }

        // 完毕
        LoggerUtil.log(0, "*** 输出创建完毕 ***");
    }

    /**
     * Apply the results of boolean operation to the input MeshGroup.
     */
    public ModelInstance getNewModelInstance() {

        // 临时保存原变量
        ModelInstance instance_old = this.instance;
        List<MeshGroup.MeshNodePart> meshNodeParts_old = this.meshNodeParts;

        // 覆盖原变量
        this.instance = instance.copy();
        this.meshNodeParts = new ArrayList<>();
        Collection<MeshGroup> meshGroups = MeshGroup.getMeshGroups(instance).values();
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts_old) {
            boolean isCopied = false;
            for (MeshGroup newMeshGroup : meshGroups) {
                for (MeshGroup.MeshNodePart newMeshNodePart : newMeshGroup.meshNodeParts) {
                    if (meshNodePart.meshPart.id.equals(newMeshNodePart.meshPart.id)) {
                        newMeshNodePart.userObject = meshNodePart.userObject;
                        meshNodeParts.add(newMeshNodePart);
                        isCopied = true;
                        break;
                    }
                }
                if (isCopied) break;
            }
            assert (isCopied);
        }

        // 调用原apply函数
        apply();

        // 还原临时保存的原变量
        ModelInstance newInstance = instance;
        this.instance = instance_old;
        this.meshNodeParts = meshNodeParts_old;

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
     * @param solid       solid object used to fill the arrays
     * @param vertices    vertices array to be filled
     * @param indices     indices array to be filled
     * @param datas       datas array to be filled
     * @param faceStatus1 a status expected for the faces used to to fill the data arrays
     * @param faceStatus2 a status expected for the faces used to to fill the data arrays
     */
    private void groupObjectComponents(Solid solid, ArrayList<Vertex> vertices, ArrayList<Integer> indices, ArrayList<VertexData> datas, int faceStatus1, int faceStatus2) {
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
        Vector3 center = tmpV.set(meshPart.center).mul(transform);

        if (center.x > bound.xMax + radius || center.x < bound.xMin - radius) return false;
        if (center.y > bound.yMax + radius || center.y < bound.yMin - radius) return false;
        return !(center.z > bound.zMax + radius) && !(center.z < bound.zMin - radius);
    }

    //-------------------------- Inner Class --------------------------------------------//

    /**
     * Class to store the operation data in each boolean operation pairs.
     */
    public class BoolPair {
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
                        vertexMixer.addVertex(id, data.values, tmpV);
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
