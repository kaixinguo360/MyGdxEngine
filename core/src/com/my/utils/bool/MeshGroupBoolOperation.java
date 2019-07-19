package com.my.utils.bool;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.MeshGroup;
import com.my.utils.MyLogger;
import com.my.utils.VertexMixer;

import java.util.ArrayList;

/**
 * Class used to apply boolean operations on MeshGroup.
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
public class MeshGroupBoolOperation implements Cloneable
{
    public final static short UNKNOWN = BooleanUtils.UNKNOWN;
    public final static short DIFF = BooleanUtils.DIFF;
    public final static short INTER = BooleanUtils.INTER;
    public final static short UNION = BooleanUtils.UNION;

    /** 待操作MeshGroup */
    private MeshGroup meshGroup1;

    /** 操作类型 **/
    private int type = UNKNOWN;

    /** 待操作MeshPart计数 **/
    private int count = 0;

    /** 参考MyNodePart */
    private MeshGroup.MyNodePart nodePart2; //TODO: 直接使用MeshGroup而不是MyNodePart到来创建参考Object3D

    /** 初始化attrUtils **/
    VertexMixer vertexMixer = new VertexMixer();

	//--------------------------------CONSTRUCTORS----------------------------------//

	/**
	 * Constructs a MeshGroupBoolOperation object to apply boolean operation in two solids.
	 * Makes preliminary calculations
	 *
	 * @param meshGroup1 first meshGroup where boolean operations will be applied
	 * @param transform1 transform of the first mesh
	 * @param meshGroup2 second meshGroup where boolean operations will be applied
	 * @param transform2 transform of the second mesh
	 */
	public MeshGroupBoolOperation(MeshGroup meshGroup1, Matrix4 transform1, MeshGroup meshGroup2, Matrix4 transform2) throws BooleanOperationException {
        MyLogger.log(0, "\n*** 开始创建MeshPartBoolUtil ***");

        this.meshGroup1 = meshGroup1;

        //设置attrUtils
        MyLogger.log(0, "设置输出属性...");
        configAttrUtils(meshGroup1.mesh.getVertexAttributes(), meshGroup2.mesh.getVertexAttributes());

        //获取NodePart2
        MyLogger.log(0, "设置参考物体...");
        for(MeshGroup.MyNodePart nodePart : meshGroup2.myNodeParts) {
            nodePart2 = nodePart;
            break;
        }
        if(nodePart2 == null) {
            throw new RuntimeException("Error Input");
        }
        MyLogger.log(0, "参考物体MeshPart: " + nodePart2.meshPart.id);

        //获取m2
        Matrix4 m2 = transform2.cpy().mul(nodePart2.node.calculateLocalTransform());

        //Create object2
        Object3D object2Base = new Object3D(nodePart2.meshPart, m2);
        Bound bound2 = object2Base.getBound();

        //representation to apply boolean operations
        MyLogger.log(0, "设置待操作meshPart...");
        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
            MyLogger.log(0, "处理meshPart: " + myNodePart.meshPart.id);

            Matrix4 m1 = transform1.cpy().mul(myNodePart.node.calculateLocalTransform());

            myNodePart.meshPart.update();
            if(!isOverlap(myNodePart.meshPart, m1, bound2)) {
                MyLogger.log(0, " ->| 终止 (边界盒未覆盖)");
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
                if(!object1.classifyFaces(object2)) {
                    throw new BooleanOperationException("Error!", myNodePart.meshPart, nodePart2.meshPart);
                }

                MyLogger.log(0, " -> 正在分类面2");
                if(!object2.classifyFaces(object1)) {
                    throw new BooleanOperationException("Error!", nodePart2.meshPart, myNodePart.meshPart);
                }

                MyPair pair = new MyPair(object1, object2);
                pair.m1 = m1.inv();
                myNodePart.userObject = pair;

                count++;
            } else {
                MyLogger.log(0, " ->| 终止 (没有相交面)");
            }
            MyLogger.log(0, "meshPart[" + myNodePart.meshPart.id + "] 处理完毕!\n");
        }
        MyLogger.log(1, "处理MeshPart[" + count + "]个");

        MyLogger.log(0, "*** 输入设置完毕 ***\n");
	}

    /**
     * Configure VertexMixer
     *
     * @param a1 first VertexAttributes add to VertexMixer
     * @param a2 second VertexAttributes add to VertexMixer
     */
	public void configAttrUtils(VertexAttributes a1, VertexAttributes a2) {
		vertexMixer.addAttributes(a1);
		vertexMixer.addAttributes(a2);
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
	public void doUnion()
	{
        type = UNION;
        MyLogger.log(0, "运行doUnion...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return;
        }

	    for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;
            pair.verData = composeMesh(object1, object2, Face.OUTSIDE, Face.SAME, Face.OUTSIDE);
        }
	}


    /**
     * Do intersection operation between the input MeshGroups, save the results to each MyPair.
     */
	public void doIntersection()
	{
        type = INTER;
        MyLogger.log(0, "运行doIntersection...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return;
        }

        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;
            pair.verData = composeMesh(object1, object2, Face.INSIDE, Face.SAME, Face.INSIDE);
        }
	}

	/**
     * Do difference operation between the input MeshGroups, save the results to each MyPair.
	 */
	public void doDifference()
	{
        type = DIFF;
        MyLogger.log(0, "运行doDifference...");

        if(count == 0){
            MyLogger.log(0, "待操作MeshPart为0, 直接跳过");
            return;
        }

        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
            if(myNodePart.userObject == null) continue;

            MyPair pair = (MyPair) myNodePart.userObject;
            Object3D object1 = pair.object1;
            Object3D object2 = pair.object2;

            object2.invertInsideFaces();
            pair.verData = composeMesh(object1, object2, Face.OUTSIDE, Face.OPPOSITE, Face.INSIDE);
            object2.invertInsideFaces();
        }
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
        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
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
        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
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
        for(MeshGroup.MyNodePart myNodePart : meshGroup1.myNodeParts) {
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

                if(data.getMesh() == meshGroup1.mesh) {
                    vertexMixer.addVertex(0, data.getData(), tmpV);
                } else if(data.getMesh() == nodePart2.mesh) {
                    vertexMixer.addVertex(1, data.getData(), tmpV);
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















