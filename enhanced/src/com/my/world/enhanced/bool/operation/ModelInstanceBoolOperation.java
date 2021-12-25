package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.my.world.enhanced.bool.util.MeshGroup;
import com.my.world.enhanced.bool.util.MyLogger;
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
public class ModelInstanceBoolOperation implements Cloneable
{
    public final static short UNKNOWN = 0;
    public final static short DIFF = 1;
    public final static short INTER = 2;
    public final static short UNION = 4;

    /** 待操作ModelInstance */
    private ModelInstance instance;

    /** 待操作MeshGroups */
    private Map<Mesh, MeshGroup> meshGroups;

    /** 待操作MyNodeParts */
    private Array<MeshGroup.MyNodePart> myNodeParts = new Array<>();

    /** 待操作Mesh序号 */
    private Map<Mesh, Integer> mapIds = new HashMap<>();

    /** 操作类型 **/
    private int type = UNKNOWN;

    /** 待操作MeshPart计数 **/
    private int count = 0;

    /** 是否应该跳过 **/
    public boolean skip = false;

    /** 参考MyNodePart */
    private MeshPart referenceMeshPart; //TODO: 直接使用MeshGroup而不是MeshPart到来创建参考Object3D

    /** 初始化attrUtils **/
    VertexMixer vertexMixer = new VertexMixer();

	//--------------------------------CONSTRUCTORS----------------------------------//

	/**
	 * Constructs a MeshGroupBoolOperation object to apply boolean operation in two solids.
	 * Makes preliminary calculations
	 *
	 * @param instance ModelInstance where boolean operations will be applied
	 * @param reference refer MeshPart where boolean operations will be applied
	 * @param transform2 transform of the second mesh
	 */
	public ModelInstanceBoolOperation(ModelInstance instance,
                                      MeshPart reference,
                                      Matrix4 transform2) throws BooleanOperationException {
        MyLogger.log(0, "\n*** 开始创建ModelInstanceBoolUtil ***");

        //保存ModelInstance
        this.instance = instance;

        //获取ModelInstance的MeshGroup
        this.meshGroups = MeshGroup.getMeshGroupsFromModelInstance(instance);

        //获取参考物体的NodePart
        referenceMeshPart = reference;
        MyLogger.log(0, "参考物体MeshPart: " + referenceMeshPart.id);

        //获取参考物体变换m2
        Matrix4 m2 = transform2.cpy();

        //创建参考物体object2
        Object3D object2Base = new Object3D(referenceMeshPart, m2);
        Bound bound2 = object2Base.getBound();

        //开始遍历meshGroups
        int id = 0;
        for(MeshGroup meshGroup : meshGroups.values()) {
            MyLogger.log(0, "设置meshGroup" + 1);

            MyLogger.log(0, "设置输出属性...");
            addMesh(meshGroup.mesh);

            //representation to apply boolean operations
            MyLogger.log(0, "设置待操作meshPart...");
            for(MeshGroup.MyNodePart myNodePart : meshGroup.myNodeParts) {
                myNodeParts.add(myNodePart);

                MyLogger.log(0, "处理meshPart: " + myNodePart.meshPart.id);

                Matrix4 m1 = instance.transform.cpy().mul(myNodePart.node.calculateLocalTransform());

                myNodePart.meshPart.update();
                if(!isOverlap(myNodePart.meshPart, m1, bound2)) {
                    MyLogger.log(0, " ->| 终止 (边界盒未覆盖)"); // TODO: 会错误的略过未相交的meshPart
                    continue;
                }

                MyLogger.log(0, " -> 正在创建Object3D");
                Object3D object1 = new Object3D(myNodePart.meshPart, m1);
                Object3D object2 = (Object3D) object2Base.clone();

                //split the faces so that none of them intercepts each other
                MyLogger.log(0, " -> 正在分割面1");
                boolean isSplit1, isSplit2;
                isSplit1 = object1.splitFaces(object2);
                MyLogger.log(0, " -> 正在分割面2");
                isSplit2 = object2.splitFaces(object1);

                if(isSplit1 || isSplit2) {

                    //classify faces as being inside or outside the other solid

                    MyLogger.log(0, " -> 正在分类面1");
                    object1.classifyFaces(object2);

                    MyLogger.log(0, " -> 正在分类面2");
                    object2.classifyFaces(object1);

                    MyPair pair = new MyPair(object1, object2);
                    pair.m1 = m1.inv();
                    myNodePart.userObject = pair;

                    count++;
                } else {
                    MyLogger.log(0, " ->| 终止 (没有相交面)");
                }
                MyLogger.log(0, "MeshPart[" + myNodePart.meshPart.id + "] 处理完毕!\n");
            }
            MyLogger.log(0, "Mesh[" + meshGroup.mesh + "] 处理完毕!\n");
            id++;
        }
        MyLogger.log(1, "处理Mesh[" + id + "]个, MeshPart[" + count + "]个");

        //设置AttrProvider
        setAttrProvider();
        addMesh(referenceMeshPart.mesh);

        if (count == 0) {
            skip = true;
        }

        MyLogger.log(0, "*** 输入设置完毕 ***\n");
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
                        //No Thing In Here
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
     * Do union operation between the input MeshGroups, save the results to each MyPair.
     */
	public boolean doUnion()
	{
        type = UNION;
        MyLogger.log(0, "运行doUnion...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

	    for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;
            pair.verData = composeMesh(object1, object2, Face.OUTSIDE, Face.SAME, Face.OUTSIDE);
        }

        return true;
	}


    /**
     * Do intersection operation between the input MeshGroups, save the results to each MyPair.
     */
	public boolean doIntersection()
	{
        type = INTER;
        MyLogger.log(0, "运行doIntersection...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;
            pair.verData = composeMesh(object1, object2, Face.INSIDE, Face.SAME, Face.INSIDE);
        }

        return true;
	}

	/**
     * Do difference operation between the input MeshGroups, save the results to each MyPair.
	 */
	public boolean doDifference()
	{
        type = DIFF;
        MyLogger.log(0, "运行doDifference...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return false;
        }

        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;

            object2.invertInsideFaces();
            pair.verData = composeMesh(object1, object2, Face.OUTSIDE, Face.OPPOSITE, Face.INSIDE);
            object2.invertInsideFaces();
        }

        return true;
	}


    //-------------------------------OUTPUT-----------------------------//

    /**
     * Get the type of this boolean operation.
     * @return The type of this boolean operation
     */
    public int getType() {
        return type;
    }

    /**
     * Apply the results of boolean operation to the input MeshGroup.
     */
    public void apply() {
        MyLogger.log(0, "\n*** 开始创建输出 ***");

        //获取总顶点数, 总索引数, 目标顶点大小
        int allVerNum = 0;
        int allIndexNum = 0;
        int verSize = vertexMixer.getTargetVertexSize();
        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyVerData verData = ((MyPair) myNodePart.userObject).verData;
            //如果verData为空
            if(verData == null) {
                continue;
            }
            allVerNum += verData.vertexNum;
            allIndexNum += verData.indexNum;
        }

        //新建对应数组
        //TODO: 如果输出顶点数大于short类型最大值, 则划分成多个Mesh
        MyLogger.log(0, "总顶点数: " + allVerNum);
        MyLogger.log(0, "总索引数: " + allIndexNum);
        assert ((allVerNum) < Short.MAX_VALUE) : "Too Many Vertexes!: " + allVerNum;
        float[] vs = new float[verSize * allVerNum];
        short[] is = new short[allIndexNum];

        int vsOffset = 0;
        int isOffset = 0;
        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            MyVerData verData = pair.verData;

            //如果verData为空
            if(verData == null) {
                continue;
            }

            verData.copyToArray(vs, vsOffset, is, isOffset, pair.m1);

            vsOffset += verData.vertexNum;
            isOffset += verData.indexNum;

            MyLogger.log(0, "meshPart[" + myNodePart.meshPart.id + "]: 顶点: " + verData.vertexNum + ", 索引: " + verData.indexNum);
            MyLogger.log(0, "meshPart[" + myNodePart.meshPart.id + "]: vsOffset: " + vsOffset + ", isOffset: " + isOffset);


//            myNodePart.node.localTransform.set(pair.m1.inv());
//            myNodePart.node.isAnimated = true;
        }

        assert (vsOffset == allVerNum) : "顶点数不符合!";
        assert (isOffset == allIndexNum) : "索引数不符合!";

        //使用对应数组创建Mesh
        Mesh mesh = new Mesh(false, allVerNum, allIndexNum, vertexMixer.getTargetAttr());
        mesh.setVertices(vs);
        mesh.setIndices(is);

        //令每一个MeshPart使用新Mesh
        int tmpOffset = 0;
        for(MeshGroup.MyNodePart myNodePart : myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyVerData verData = ((MyPair) myNodePart.userObject).verData;

            //如果verData为空
            if(verData == null) {
                MyLogger.log(0, "删除meshPart: " + myNodePart.meshPart.id);
                myNodePart.node.parts.removeValue(myNodePart.nodePart, true);
            } else {
                MyLogger.log(0, "更新meshPart: " + myNodePart.meshPart.id);
                MeshPart meshPart = myNodePart.meshPart;
                MyLogger.log(0, "tmpOffset: " + tmpOffset);
                MyLogger.log(0, "indexNum: " + verData.indexNum);
                meshPart.set(meshPart.id, mesh, tmpOffset, verData.indexNum, meshPart.primitiveType);
                meshPart.update();
//            myNodePart.node.localTransform.translate(meshPart.center.cpy().scl(-1));
//            meshPart.center.set(0, 0, 0);
                tmpOffset += verData.indexNum;
            }
        }

        //完毕
        MyLogger.log(0, "*** 输出创建完毕 ***\n");
    }

    /**
     * Apply the results of boolean operation to the input MeshGroup.
     */
    public ModelInstance getNewModelInstance() {
        MyLogger.log(0, "\n*** 开始创建输出 ***");

        //临时保存原变量
        ModelInstance instance_old = this.instance;
        Map<Mesh, MeshGroup> meshGroups_old = this.meshGroups;
        Array<MeshGroup.MyNodePart> myNodeParts_old = this.myNodeParts;

        //覆盖原变量
        this.instance = instance.copy();
        this.meshGroups = MeshGroup.getMeshGroupsFromModelInstance(instance);
        for(MeshGroup meshGroup : meshGroups_old.values()) {
            MeshGroup newMeshGroup = meshGroups.get(meshGroup.mesh);
            assert (newMeshGroup != null);
            boolean isCopied;
            for(MeshGroup.MyNodePart myNodePart : meshGroup.myNodeParts) {
                isCopied = false;;
                for(MeshGroup.MyNodePart newMyNodePart : newMeshGroup.myNodeParts) {
                    if(!myNodePart.meshPart.id.equals(newMyNodePart.meshPart.id))
                        continue;
                    newMyNodePart.userObject = myNodePart.userObject;
                    isCopied = true;
                    break;
                }
                assert (isCopied);
            }
        }
        this.myNodeParts = new Array<>();
        for(MeshGroup.MyNodePart myNodePart : myNodeParts_old) {
            for(MeshGroup newMeshGroup : meshGroups.values()) {
                for(MeshGroup.MyNodePart newMyNodePart : newMeshGroup.myNodeParts) {
                    if(!myNodePart.meshPart.id.equals(newMyNodePart.meshPart.id))
                        continue;
                    myNodeParts.add(newMyNodePart);
                    break;
                }
            }
        }
        assert (myNodeParts.size == this.myNodeParts.size);

        //调用原apply函数
        apply();
        ModelInstance newInstance = instance;

        //还原临时保存的原变量
        this.instance = instance_old;
        this.meshGroups = meshGroups_old;
        this.myNodeParts = myNodeParts_old;

        return newInstance;
    }

	//--------------------------PRIVATES--------------------------------------------//

	/**
	 * Composes a Mesh based on the faces status of the two operators solids:
	 * Face.INSIDE, Face.OUTSIDE, Face.SAME, Face.OPPOSITE
	 *
	 * @param faceStatus1 status expected for the first solid faces
	 * @param faceStatus2 other status expected for the first solid faces
	 * (expected a status for the faces coincident with second solid faces)
	 * @param faceStatus3 status expected for the second solid faces
	 */
	private MyVerData composeMesh(Object3D object1, Object3D object2, int faceStatus1, int faceStatus2, int faceStatus3)
	{
	    MyLogger.log(0, "运行composeMesh...");

		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<Integer> indices = new ArrayList<>();
		ArrayList<VertexData> datas = new ArrayList<>();

		//group the elements of the two solids whose faces fit with the desired status
		groupObjectComponents(object1, vertices, indices, datas, faceStatus1, faceStatus2);
		groupObjectComponents(object2, vertices, indices, datas, faceStatus3, faceStatus3);

        if(indices.size() == 0) {
            return null;
        }

		//returns the solid containing the grouped elements
		return new MyVerData(vertices, indices, datas);
	}

	/**
	 * Fills solid arrays with data about faces of an object generated whose status
	 * is as required
	 *
	 * @param object solid object used to fill the arrays
	 * @param vertices vertices array to be filled
	 * @param indices indices array to be filled
	 * @param datas datas array to be filled
	 * @param faceStatus1 a status expected for the faces used to to fill the data arrays
	 * @param faceStatus2 a status expected for the faces used to to fill the data arrays
	 */
	private void groupObjectComponents(Object3D object, ArrayList<Vertex> vertices, ArrayList<Integer> indices, ArrayList<VertexData> datas, int faceStatus1, int faceStatus2)
	{
        MyLogger.log(0, "运行groupObjectComponents...");

		Face face;
		//for each face..
		for(int i=0;i<object.getNumFaces();i++)
		{
			face = object.getFace(i);
			//if the face status fits with the desired status...
			if(face.getStatus()==faceStatus1 || face.getStatus()==faceStatus2)
			{
				//adds the face elements into the arrays
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
     * @param meshPart input MeshPart
     * @param transform transform of the input MeshPart
     * @param bound input Bound
     * @return is overlapped or not
     */
    private boolean isOverlap(MeshPart meshPart, Matrix4 transform, Bound bound) {
        float radius = meshPart.radius;
        Vector3 center = new Vector3(meshPart.center).mul(transform);

        if(center.x > bound.xMax + radius || center.x < bound.xMin - radius) return false;
        if(center.y > bound.yMax + radius || center.y < bound.yMin - radius) return false;
        if(center.z > bound.zMax + radius || center.z < bound.zMin - radius) return false;

        return true;
    }

    //-------------------------- Inner Class --------------------------------------------//

    /**
     * Class to store the operation data in each boolean operation pairs.
     */
    private class MyPair {
        private final Object3D object1, object2;
        private Matrix4 m1, m2;
        private MyVerData verData;
        private float[] vs;
        private short[] is;

        private MyPair(Object3D object1, Object3D object2) {
            this.object1 = object1;
            this.object2 = object2;
        }
    }

    /**
     * Class to store the vertex data and index data.
     */
    private class MyVerData {
        private final ArrayList<Vertex> vertices;
        private final ArrayList<Integer> indexs;
        private final ArrayList<VertexData> datas;
        private final int vertexNum;
        private final int indexNum;

        private MyVerData(ArrayList<Vertex> vertices, ArrayList<Integer> indexs, ArrayList<VertexData> datas) {
            assert (vertices.size() == datas.size()) : "vertices.vertexNum() != datas.vertexNum()";
            assert (vertices.size() < Short.MAX_VALUE) : "Too Many Vertices!"; //TODO: 解决输出顶点太多, 超出short类型最大值问题

            this.vertexNum = vertices.size();
            this.indexNum = indexs.size();

            this.vertices = vertices;
            this.indexs = indexs;
            this.datas = datas;
        }

        private void copyToArray(float[] vs, int vsOffset, short[] is, int isOffset, Matrix4 transform) {
            //复制顶点数据
            Vector3 tmpV = new Vector3();
            vertexMixer.begin(vertexNum);
            for(int i = 0; i< vertexNum; i++) {
                vertices.get(i).toVector3(tmpV);
                tmpV.mul(transform);
                VertexData data = datas.get(i);

                Mesh mesh = data.getMesh();
                if(mesh != null && mapIds.containsKey(data.getMesh())) {
                    int id = mapIds.get(data.getMesh());
                    if(id >= 0)
                        vertexMixer.addVertex(id, data.getData(), tmpV);
                    else
                        vertexMixer.addVertex(tmpV);
                } else {
                    vertexMixer.addVertex(tmpV);
                }
            }
            vertexMixer.buildToArray(vs, vsOffset);

            //复制索引数据
            int max = 0,
                    min = Integer.MAX_VALUE;
            for(int i=0; i<indexs.size(); i++) {
                if(indexs.get(i) + vsOffset > Short.MAX_VALUE) {
                    throw new RuntimeException("Index Is Too Bigger!");
                }
                if(indexs.get(i) < min) min = indexs.get(i);
                if(indexs.get(i) > max) max = indexs.get(i);
                is[isOffset + i] = (short) (indexs.get(i) + vsOffset);
            }
            MyLogger.log(0, "MIN: " + (min + vsOffset) + ", MAX: " + (max + vsOffset));
        }
    }

}















