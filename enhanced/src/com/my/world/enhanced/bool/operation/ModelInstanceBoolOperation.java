package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.my.world.enhanced.bool.util.LoggerUtil;
import com.my.world.enhanced.bool.util.MeshGroup;

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

    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();

    /**
     * 是否应该跳过
     **/
    public boolean skip = false;
    /**
     * 待操作ModelInstance
     */
    private ModelInstance instance;
    /**
     * 待操作MeshGroups
     */
    private Map<Mesh, MeshGroup> meshGroups;
    /**
     * 待操作MeshNodeParts
     */
    private Array<MeshGroup.MeshNodePart> meshNodeParts = new Array<>();
    /**
     * 操作类型
     **/
    private int type = UNKNOWN;
    /**
     * 待操作MeshPart计数
     **/
    private int count = 0;

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
        LoggerUtil.log(0, "*** 布尔操作创建开始 ***");

        // Setup Reference Object
        LoggerUtil.log(0, "参考物体: " + reference.id);
        Matrix4 referenceTransform = tmpM.set(instance.transform).inv().mul(transform2);
        Solid referenceSolid = Solid.obtain(reference, referenceTransform);
        Bound referenceBound = referenceSolid.getBound();
        for (VertexAttribute attribute : reference.mesh.getVertexAttributes()) {
            if (!attributes.containsKey(attribute.usage)) {
                attributes.put(attribute.usage, attribute.copy());
            }
        }

        // Setup Target Object
        LoggerUtil.log(0, "目标物体: " + instance);
        this.instance = instance;
        this.meshGroups = MeshGroup.getMeshGroups(instance);
        LoggerUtil.log(0, "开始处理目标物体");
        for (MeshGroup meshGroup : meshGroups.values()) {
            LoggerUtil.log(0, "  开始处理 Mesh(" + meshGroup.mesh + ")");

            for (VertexAttribute attribute : meshGroup.mesh.getVertexAttributes()) {
                if (!attributes.containsKey(attribute.usage)) {
                    attributes.put(attribute.usage, attribute.copy());
                }
            }

            for (MeshGroup.MeshNodePart meshNodePart : meshGroup.meshNodeParts) {
                LoggerUtil.log(0, "    开始处理 MeshPart(" + meshNodePart.meshPart.id + ")");

                meshNodeParts.add(meshNodePart);

                Matrix4 meshNodePartTransform = meshNodePart.node.calculateLocalTransform();
                meshNodePart.meshPart.update();
                if (!isOverlap(meshNodePart.meshPart, meshNodePartTransform, referenceBound)) {
                    LoggerUtil.log(0, "    边界盒未相交, 终止"); // TODO: 会错误的略过未相交的meshPart
                    continue;
                }

                LoggerUtil.log(0, "      正在创建Solid...");
                Solid solid1 = Solid.obtain(meshNodePart.meshPart, meshNodePartTransform);
                Solid solid2 = (Solid) referenceSolid.clone();

                // split the faces so that none of them intercepts each other
                LoggerUtil.log(0, "      正在分割面1...");
                boolean isSplit1, isSplit2;
                isSplit1 = solid1.splitFaces(solid2);
                LoggerUtil.log(0, "      正在分割面2...");
                isSplit2 = solid2.splitFaces(solid1);

                if (!isSplit1 && !isSplit2) {
                    LoggerUtil.log(0, "    无相交面, 终止");
                    continue;
                }
                // classify faces as being inside or outside the other solid

                LoggerUtil.log(0, "      正在分类面1...");
                solid1.classifyFaces(solid2);

                LoggerUtil.log(0, "      正在分类面2...");
                solid2.classifyFaces(solid1);

                BoolPair pair = new BoolPair(solid1, solid2);
                pair.m1 = meshNodePartTransform.inv();
                meshNodePart.userObject = pair;

                count++;
                LoggerUtil.log(0, "    处理完成 MeshPart(" + meshNodePart.meshPart.id + ")");
            }
            LoggerUtil.log(0, "  处理完成 Mesh(" + meshGroup.mesh + ")");
        }
        LoggerUtil.log(1, "处理目标物体完成, MeshPart " + count + " 个");

        if (count == 0) {
            skip = true;
        }

        LoggerUtil.log(0, "*** 布尔操作创建结束 ***");
    }

    private final MeshBuilder meshBuilder = new MeshBuilder();
    private final Map<Integer, VertexAttribute> attributes = new HashMap<>();

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
        LoggerUtil.log(0, "运行doIntersection...");

        if (count == 0) {
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
        LoggerUtil.log(0, "运行doDifference...");

        if (count == 0) {
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
        LoggerUtil.log(0, "*** 开始创建输出 ***");
        meshBuilder.begin(new VertexAttributes(attributes.values().toArray(new VertexAttribute[0])));
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;
            BoolVerData verData = meshNodePart.userObject.verData;
            if (verData == null) continue;
            for (Integer index : verData.indexs) {
                Vertex vertex = verData.vertices.get(index);
                MeshPartBuilder.VertexInfo info = new MeshPartBuilder.VertexInfo();
                for (VertexAttribute attribute : vertex.data.attributes) {
                    float[] vs = vertex.data.values;
                    int offset = attribute.offset / 4;
                    switch (attribute.usage) {
                        case VertexAttributes.Usage.Position:
                            vertex.toVector3(tmpV);
                            tmpV.mul(meshNodePart.userObject.m1);
                            info.setPos(tmpV);
                            break;
                        case VertexAttributes.Usage.Normal:
                            info.setNor(vs[offset], vs[offset + 1], vs[offset + 2]); break;
                        case VertexAttributes.Usage.TextureCoordinates:
                            info.setUV(vs[offset], vs[offset + 1]); break;
                        case VertexAttributes.Usage.ColorPacked:
                        case VertexAttributes.Usage.ColorUnpacked:
                            info.setCol(vs[offset], vs[offset + 1], vs[offset + 2], vs[offset + 3]); break;
                        case VertexAttributes.Usage.Tangent:
                        case VertexAttributes.Usage.BiNormal:
                        case VertexAttributes.Usage.BoneWeight:
//                            LoggerUtil.log(4, "Ignored Attribute: " + attribute.alias);
                    }
                }
                if (!info.hasPosition) {
                    LoggerUtil.log(4, "[WARN] a_position not set");
                }
                meshBuilder.index(meshBuilder.vertex(info));
            }
        }
        Mesh mesh = meshBuilder.end();

        // 令每一个MeshPart使用新Mesh
        int offset = 0;
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts) {
            if (meshNodePart.userObject == null) continue;
            BoolVerData verData = meshNodePart.userObject.verData;

            // 如果verData为空
            if (verData == null) {
                LoggerUtil.log(0, "删除meshPart: " + meshNodePart.meshPart.id);
                meshNodePart.node.parts.removeValue(meshNodePart.nodePart, true);
            } else {
                LoggerUtil.log(0, "更新meshPart: " + meshNodePart.meshPart.id);
                MeshPart meshPart = meshNodePart.meshPart;
                LoggerUtil.log(0, "offset: " + offset);
                LoggerUtil.log(0, "indexNum: " + verData.indexNum);
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
        LoggerUtil.log(0, "*** 开始创建输出 ***");

        // 临时保存原变量
        ModelInstance instance_old = this.instance;
        Map<Mesh, MeshGroup> meshGroups_old = this.meshGroups;
        Array<MeshGroup.MeshNodePart> meshNodeParts_old = this.meshNodeParts;

        // 覆盖原变量
        this.instance = instance.copy();
        this.meshGroups = MeshGroup.getMeshGroups(instance);
        for (MeshGroup meshGroup : meshGroups_old.values()) {
            MeshGroup newMeshGroup = meshGroups.get(meshGroup.mesh);
            assert (newMeshGroup != null);
            boolean isCopied;
            for (MeshGroup.MeshNodePart meshNodePart : meshGroup.meshNodeParts) {
                isCopied = false;
                for (MeshGroup.MeshNodePart newMeshNodePart : newMeshGroup.meshNodeParts) {
                    if (!meshNodePart.meshPart.id.equals(newMeshNodePart.meshPart.id))
                        continue;
                    newMeshNodePart.userObject = meshNodePart.userObject;
                    isCopied = true;
                    break;
                }
                assert (isCopied);
            }
        }
        this.meshNodeParts = new Array<>();
        for (MeshGroup.MeshNodePart meshNodePart : meshNodeParts_old) {
            for (MeshGroup newMeshGroup : meshGroups.values()) {
                for (MeshGroup.MeshNodePart newMeshNodePart : newMeshGroup.meshNodeParts) {
                    if (!meshNodePart.meshPart.id.equals(newMeshNodePart.meshPart.id))
                        continue;
                    meshNodeParts.add(newMeshNodePart);
                    break;
                }
            }
        }
        assert (meshNodeParts.size == this.meshNodeParts.size);

        // 调用原apply函数
        apply();
        ModelInstance newInstance = instance;

        // 还原临时保存的原变量
        this.instance = instance_old;
        this.meshGroups = meshGroups_old;
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
    private static class BoolVerData {
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
    }

}
