package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.bool.util.LoggerUtil;

import java.util.ArrayList;

import static com.my.world.enhanced.bool.util.NumberUtil.dTOL;

/**
 * Data structure about a 3d solid to apply boolean operations in it.
 *
 * <br><br>Tipically, two 'Solid' objects are created to apply boolean operation. The
 * methods splitFaces() and classifyFaces() are called in this sequence for both objects,
 * always using the other one as parameter. Then the faces from both objects are collected
 * according their status.
 *
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Solid implements Cloneable {
    /**
     * solid vertices
     */
    private ArrayList<Vertex> vertices;
    /**
     * solid faces
     */
    private ArrayList<Face> faces;
    /**
     * object representing the solid extremes
     */
    private Bound bound;

    //----------------------------------CONSTRUCTOR---------------------------------//

    /**
     * Constructs a Solid object based on a mesh.
     *
     * @param mesh      mesh used to construct the Solid object
     * @param transform transform of the mesh
     */
    public Solid(Mesh mesh, Matrix4 transform) {
        Vector3 tmpV = new Vector3();
        Vertex v1, v2, v3, vertex;
        vertices = new ArrayList<>();

        // 获取顶点属性
        int offsetPosition = 0; // 获取顶点偏移: 位置
        for (VertexAttribute attribute : mesh.getVertexAttributes()) {
            int offset = attribute.offset / 4;
            if (attribute.usage == VertexAttributes.Usage.Position) {
                offsetPosition = offset;
            }
        }
        // 顶点属性获取完毕

        // 获取顶点
        int vertexSize = mesh.getVertexSize() / 4; // 顶点大小 - Mesh
        int numVertices = mesh.getNumVertices(); // 顶点数量 - Mesh
        float[] vers = new float[vertexSize * numVertices]; // 读取全部顶点数组 - Mesh
        mesh.getVertices(vers);
        VectorD[] verticesPoints = new VectorD[numVertices];
        ArrayList<Vertex> verticesTemp = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            int offsetVertex = i * vertexSize,
                    x = offsetVertex + offsetPosition,
                    y = offsetVertex + offsetPosition + 1,
                    z = offsetVertex + offsetPosition + 2;

            // 获取顶点xyz坐标
            tmpV.set(vers[x], vers[y], vers[z]).mul(transform);
            VectorD pos = new VectorD().set(tmpV);

            // 复制顶点数据到MyData对象
            float[] dataArray = new float[vertexSize];
            System.arraycopy(vers, offsetVertex, dataArray, 0, vertexSize);
            VertexData data = new VertexData(dataArray, mesh.getVertexAttributes());

            verticesPoints[i] = pos;
            vertex = addVertex(pos, data, Vertex.UNKNOWN);
            verticesTemp.add(vertex);
        }
        // 获取顶点完毕

        // 获取索引
        int numIndices = mesh.getNumIndices(); // 索引数量 - Mesh
        short[] indicesFromMesh = new short[numIndices]; // 读取全部索引数组 - Mesh
        mesh.getIndices(indicesFromMesh);
        int[] indices = new int[indicesFromMesh.length];
        for (int i = 0; i < indicesFromMesh.length; i++) {
            indices[i] = indicesFromMesh[i];
        }
        // indices数组获取完毕

        // create faces
        faces = new ArrayList<>();
        for (int i = 0; i < indices.length; i = i + 3) {
            v1 = verticesTemp.get(indices[i]);
            v2 = verticesTemp.get(indices[i + 1]);
            v3 = verticesTemp.get(indices[i + 2]);
            addFace(v1, v2, v3);
        }

        // create bound
        bound = new Bound(verticesPoints);
    }

    /**
     * Constructs a Solid object based on a mesh.
     *
     * @param meshPart  meshPart used to construct the Solid object
     * @param transform transform of the mesh
     */
    public Solid(MeshPart meshPart, Matrix4 transform) {
        assert (meshPart.primitiveType == GL20.GL_TRIANGLES) : "meshPart.primitiveType Not Equals 'GL20.GL_TRIANGLES' !";

        Vector3 tmpV = new Vector3();
        Vertex v1, v2, v3, vertex;
        vertices = new ArrayList<>();

        // 获取Mesh
        Mesh mesh = meshPart.mesh;

        // 获取顶点属性
        int offsetPosition = 0; // 获取顶点偏移: 位置
        for (VertexAttribute attribute : mesh.getVertexAttributes()) {
            int offset = attribute.offset / 4;
            if (attribute.usage == VertexAttributes.Usage.Position) {
                offsetPosition = offset;
            }
        }
        // 顶点属性获取完毕

        // 获取顶点
        int vertexSize = mesh.getVertexSize() / 4; // 顶点大小 - Mesh
        int numVertices = mesh.getNumVertices(); // 顶点数量 - Mesh
        float[] vers = new float[vertexSize * numVertices]; // 读取全部顶点数组 - Mesh
        mesh.getVertices(vers);
        VectorD[] verticesPoints = new VectorD[numVertices];
        ArrayList<Vertex> verticesTemp = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            int offsetVertex = i * vertexSize,
                    x = offsetVertex + offsetPosition,
                    y = offsetVertex + offsetPosition + 1,
                    z = offsetVertex + offsetPosition + 2;

            // 获取顶点xyz坐标
            tmpV.set(vers[x], vers[y], vers[z]).mul(transform);
            VectorD pos = new VectorD().set(tmpV);

            // 复制顶点数据到MyData对象
            float[] dataArray = new float[vertexSize];
            System.arraycopy(vers, offsetVertex, dataArray, 0, vertexSize);
            VertexData data = new VertexData(dataArray, mesh.getVertexAttributes());

            verticesPoints[i] = pos;
            vertex = addVertex(pos, data, Vertex.UNKNOWN);
            verticesTemp.add(vertex);
        }
        // 获取顶点完毕

        // 获取索引
        int offsetIndices = meshPart.offset; // 索引偏移 - Mesh
        int numIndices = meshPart.size; // 索引数量 - Mesh
        short[] indicesFromMesh = new short[numIndices]; // 读取全部索引数组 - Mesh
        mesh.getIndices(offsetIndices, numIndices, indicesFromMesh, 0);
        int[] indices = new int[indicesFromMesh.length];
        for (int i = 0; i < indicesFromMesh.length; i++) {
            indices[i] = indicesFromMesh[i];
        }
        // indices数组获取完毕

        // create faces
        faces = new ArrayList<>();
        for (int i = 0; i < indices.length; i = i + 3) {
            v1 = verticesTemp.get(indices[i]);
            v2 = verticesTemp.get(indices[i + 1]);
            v3 = verticesTemp.get(indices[i + 2]);
            addFace(v1, v2, v3);
        }

        // create bound
        bound = new Bound(verticesPoints);
    }

    //-----------------------------------OVERRIDES----------------------------------//

    /**
     * Clones the Solid object
     *
     * @return cloned Solid object
     */
    public Object clone() {
        try {
            Solid clone = (Solid) super.clone();
            clone.vertices = new ArrayList<>();
            for (int i = 0; i < vertices.size(); i++) {
                clone.vertices.add((Vertex) vertices.get(i).clone());
            }
            clone.faces = new ArrayList<>();
            for (int i = 0; i < faces.size(); i++) {
                clone.faces.add((Face) faces.get(i).clone());
            }
            clone.bound = bound;

            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //--------------------------------------GETS------------------------------------//

    /**
     * Gets the number of faces
     *
     * @return number of faces
     */
    public int getNumFaces() {
        return faces.size();
    }

    /**
     * Gets a face reference for a given position
     *
     * @param index required face position
     * @return face reference , null if the position is invalid
     */
    public Face getFace(int index) {
        if (index < 0 || index >= faces.size()) {
            return null;
        } else {
            return faces.get(index);
        }
    }

    /**
     * Gets the solid bound
     *
     * @return solid bound
     */
    public Bound getBound() {
        return bound;
    }

    //------------------------------------ADDS----------------------------------------//

    /**
     * Method used to add a face properly for internal methods
     *
     * @param v1 a face vertex
     * @param v2 a face vertex
     * @param v3 a face vertex
     */
    private Face addFace(Vertex v1, Vertex v2, Vertex v3) {
        if (!(v1.equals(v2) || v1.equals(v3) || v2.equals(v3))) {
            Face face = new Face(v1, v2, v3);
            if (face.getArea() > dTOL) {
                faces.add(face);
                return face;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Method used to add a vertex properly for internal methods
     *
     * @param pos    vertex position
     * @param data   vertex data
     * @param status vertex status
     * @return the vertex inserted (if a similar vertex already exists, this is returned)
     */
    private Vertex addVertex(VectorD pos, VertexData data, int status) {
        int i;
        // if already there is an equal vertex, it is not inserted
        Vertex vertex = new Vertex(pos, data, status);
        for (i = 0; i < vertices.size(); i++) {
            if (vertex.equals(vertices.get(i))) break;
        }
        if (i == vertices.size()) {
            vertices.add(vertex);
            return vertex;
        } else {
            vertex = vertices.get(i);
            vertex.setStatus(status);
            return vertex;
        }

    }

    private Vertex addVertex(VectorD pos, Face face, int status) {
        Vertex vertex = addVertex(pos, face.v1.getData(), status);
        vertex.avg(face.v1, face.v2, face.v3);
        return vertex;
    }

    //-------------------------FACES_SPLITTING_METHODS------------------------------//

    /**
     * Split faces so that none face is intercepted by a face of other object
     *
     * @param solid the other solid used to make the split
     */
    public boolean splitFaces(Solid solid) {
        Line line;
        Face face1, face2;
        Segment[] segments;
        Segment segment1;
        Segment segment2;
        double distFace1Vert1, distFace1Vert2, distFace1Vert3, distFace2Vert1, distFace2Vert2, distFace2Vert3;
        int signFace1Vert1, signFace1Vert2, signFace1Vert3, signFace2Vert1, signFace2Vert2, signFace2Vert3;
        int numFacesBefore = getNumFaces();
        int numFacesStart = getNumFaces();
        int numFacesMax = (numFacesStart > solid.getNumFaces()) ? numFacesStart : solid.getNumFaces();
        int facesIgnored = 0;
        int repeat = 0; // 防止死循环
        boolean isChanged = false;

        // if the objects bounds overlap...
        if (getBound().overlap(solid.getBound())) {
            isChanged = true;

            // for each object1 face...
            for (int i = 0; i < getNumFaces(); i++) {
                // if object1 face bound and object2 bound overlap ...
                face1 = getFace(i);

                numFacesBefore = getNumFaces(); // 防止死循环

                if (face1.getBound().overlap(solid.getBound())) {
                    // for each object2 face...
                    for (int j = 0; j < solid.getNumFaces(); j++) {
                        // if object1 face bound and object2 face bound overlap...
                        face2 = solid.getFace(j);
                        if (face1.getBound().overlap(face2.getBound())) {
                            // PART I - DO TWO POLIGONS INTERSECT?
                            // POSSIBLE RESULTS: INTERSECT, NOT_INTERSECT, COPLANAR

                            // distance from the face1 vertices to the face2 plane
                            distFace1Vert1 = computeDistance(face1.v1, face2);
                            distFace1Vert2 = computeDistance(face1.v2, face2);
                            distFace1Vert3 = computeDistance(face1.v3, face2);

                            // distances signs from the face1 vertices to the face2 plane
                            signFace1Vert1 = (distFace1Vert1 > dTOL ? 1 : (distFace1Vert1 < -dTOL ? -1 : 0));
                            signFace1Vert2 = (distFace1Vert2 > dTOL ? 1 : (distFace1Vert2 < -dTOL ? -1 : 0));
                            signFace1Vert3 = (distFace1Vert3 > dTOL ? 1 : (distFace1Vert3 < -dTOL ? -1 : 0));

                            // if all the signs are zero, the planes are coplanar
                            // if all the signs are positive or negative, the planes do not intersect
                            // if the signs are not equal...
                            if (!(signFace1Vert1 == signFace1Vert2 && signFace1Vert2 == signFace1Vert3)) {
                                // distance from the face2 vertices to the face1 plane
                                distFace2Vert1 = computeDistance(face2.v1, face1);
                                distFace2Vert2 = computeDistance(face2.v2, face1);
                                distFace2Vert3 = computeDistance(face2.v3, face1);

                                // distances signs from the face2 vertices to the face1 plane
                                signFace2Vert1 = (distFace2Vert1 > dTOL ? 1 : (distFace2Vert1 < -dTOL ? -1 : 0));
                                signFace2Vert2 = (distFace2Vert2 > dTOL ? 1 : (distFace2Vert2 < -dTOL ? -1 : 0));
                                signFace2Vert3 = (distFace2Vert3 > dTOL ? 1 : (distFace2Vert3 < -dTOL ? -1 : 0));

                                // if the signs are not equal...
                                if (!(signFace2Vert1 == signFace2Vert2 && signFace2Vert2 == signFace2Vert3)) {
                                    line = new Line(face1, face2);

                                    // intersection of the face1 and the plane of face2
                                    segment1 = new Segment(line, face1, signFace1Vert1, signFace1Vert2, signFace1Vert3);

                                    // intersection of the face2 and the plane of face1
                                    segment2 = new Segment(line, face2, signFace2Vert1, signFace2Vert2, signFace2Vert3);

                                    // if the two segments intersect...
                                    if (segment1.intersect(segment2)) {
                                        // PART II - SUBDIVIDING NON-COPLANAR POLYGONS
                                        int lastNumFaces = getNumFaces();
                                        this.splitFace(i, segment1, segment2);

                                        // prevent from infinite loop (with a loss of faces...)
                                        if (numFacesMax * 3 < getNumFaces()) // 防止死循环
                                        {
                                            LoggerUtil.log(2, "possible infinite loop situation: terminating faces split - Too Many Face: " + getNumFaces() + " (Start: " + numFacesMax + ")");
                                            return isChanged;
                                        }

                                        // if the face in the position isn't the same, there was a break
                                        if (face1 != getFace(i)) {
                                            // if the generated solid is equal the origin...
                                            if (face1.equals(getFace(getNumFaces() - 1))) {
                                                // return it to its position and jump it
                                                if (i != (getNumFaces() - 1)) {
                                                    faces.remove(getNumFaces() - 1);
                                                    faces.add(i, face1);
                                                } else {
                                                    continue;
                                                }
                                            }
                                            // else: test next face
                                            else {
                                                if (numFacesBefore == getNumFaces()) { // 防止死循环
                                                    repeat++;
                                                    if (repeat >= 100) {
                                                        // 重复循环次数超出忍耐, 直接跳出
                                                        LoggerUtil.log(2, "possible infinite loop situation: terminating faces split - Too Many Repeat");
                                                        return isChanged;
                                                    }
                                                }
                                                i--;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return isChanged;
    }

    /**
     * Computes closest distance from a vertex to a plane
     *
     * @param vertex vertex used to compute the distance
     * @param face   face representing the plane where it is contained
     * @return the closest distance from the vertex to the plane
     */
    private double computeDistance(Vertex vertex, Face face) {
        VectorD normal = face.getNormal();
        double a = normal.x;
        double b = normal.y;
        double c = normal.z;
        double d = -(a * face.v1.x + b * face.v1.y + c * face.v1.z);
        return a * vertex.x + b * vertex.y + c * vertex.z + d;
    }

    /**
     * Split an individual face
     *
     * @param facePos  face position on the array of faces
     * @param segment1 segment representing the intersection of the face with the plane
     *                 of another face
     * @return segment2 segment representing the intersection of other face with the
     * plane of the current face plane
     */
    private void splitFace(int facePos, Segment segment1, Segment segment2) {
        Vertex startPosVertex, endPosVertex;
        VectorD startPos, endPos;
        int startType, endType, middleType;
        double startDist, endDist;

        Face face = getFace(facePos);
        Vertex startVertex = segment1.getStartVertex();
        Vertex endVertex = segment1.getEndVertex();

        // starting point: deeper starting point
        if (segment2.getStartDistance() > segment1.getStartDistance() + dTOL) {
            startDist = segment2.getStartDistance();
            startType = segment1.getIntermediateType();
            startPos = segment2.getStartPosition();
        } else {
            startDist = segment1.getStartDistance();
            startType = segment1.getStartType();
            startPos = segment1.getStartPosition();
        }

        // ending point: deepest ending point
        if (segment2.getEndDistance() < segment1.getEndDistance() - dTOL) {
            endDist = segment2.getEndDistance();
            endType = segment1.getIntermediateType();
            endPos = segment2.getEndPosition();
        } else {
            endDist = segment1.getEndDistance();
            endType = segment1.getEndType();
            endPos = segment1.getEndPosition();
        }
        middleType = segment1.getIntermediateType();

        // set vertex to BOUNDARY if it is start type
        if (startType == Segment.VERTEX) {
            startVertex.setStatus(Vertex.BOUNDARY);
        }

        // set vertex to BOUNDARY if it is end type
        if (endType == Segment.VERTEX) {
            endVertex.setStatus(Vertex.BOUNDARY);
        }

        // VERTEX-_______-VERTEX
        if (startType == Segment.VERTEX && endType == Segment.VERTEX) {
        }

        // ______-EDGE-______
        else if (middleType == Segment.EDGE) {
            // gets the edge
            int splitEdge;
            if ((startVertex == face.v1 && endVertex == face.v2) || (startVertex == face.v2 && endVertex == face.v1)) {
                splitEdge = 1;
            } else if ((startVertex == face.v2 && endVertex == face.v3) || (startVertex == face.v3 && endVertex == face.v2)) {
                splitEdge = 2;
            } else {
                splitEdge = 3;
            }

            // VERTEX-EDGE-EDGE
            if (startType == Segment.VERTEX) {
                breakFaceInTwo(facePos, endPos, splitEdge);
            }

            // EDGE-EDGE-VERTEX
            else if (endType == Segment.VERTEX) {
                breakFaceInTwo(facePos, startPos, splitEdge);
            }

            // EDGE-EDGE-EDGE
            else if (startDist == endDist) {
                breakFaceInTwo(facePos, endPos, splitEdge);
            } else {
                if ((startVertex == face.v1 && endVertex == face.v2) || (startVertex == face.v2 && endVertex == face.v3) || (startVertex == face.v3 && endVertex == face.v1)) {
                    breakFaceInThree(facePos, startPos, endPos, splitEdge);
                } else {
                    breakFaceInThree(facePos, endPos, startPos, splitEdge);
                }
            }
        }

        // ______-FACE-______

        // VERTEX-FACE-EDGE
        else if (startType == Segment.VERTEX && endType == Segment.EDGE) {
            breakFaceInTwo(facePos, endPos, endVertex);
        }
        // EDGE-FACE-VERTEX
        else if (startType == Segment.EDGE && endType == Segment.VERTEX) {
            breakFaceInTwo(facePos, startPos, startVertex);
        }
        // VERTEX-FACE-FACE
        else if (startType == Segment.VERTEX && endType == Segment.FACE) {
            breakFaceInThree(facePos, endPos, startVertex);
        }
        // FACE-FACE-VERTEX
        else if (startType == Segment.FACE && endType == Segment.VERTEX) {
            breakFaceInThree(facePos, startPos, endVertex);
        }
        // EDGE-FACE-EDGE
        else if (startType == Segment.EDGE && endType == Segment.EDGE) {
            breakFaceInThree(facePos, startPos, endPos, startVertex, endVertex);
        }
        // EDGE-FACE-FACE
        else if (startType == Segment.EDGE && endType == Segment.FACE) {
            breakFaceInFour(facePos, startPos, endPos, startVertex);
        }
        // FACE-FACE-EDGE
        else if (startType == Segment.FACE && endType == Segment.EDGE) {
            breakFaceInFour(facePos, endPos, startPos, endVertex);
        }
        // FACE-FACE-FACE
        else if (startType == Segment.FACE && endType == Segment.FACE) {
            VectorD segmentVector = new VectorD(startPos.x - endPos.x, startPos.y - endPos.y, startPos.z - endPos.z);

            // if the intersection segment is a point only...
            if (Math.abs(segmentVector.x) < dTOL && Math.abs(segmentVector.y) < dTOL && Math.abs(segmentVector.z) < dTOL) {
                breakFaceInThree(facePos, startPos);
                return;
            }

            // gets the vertex more lined with the intersection segment
            int linedVertex;
            VectorD linedVertexPos;
            VectorD vertexVector = new VectorD(endPos.x - face.v1.x, endPos.y - face.v1.y, endPos.z - face.v1.z);
            vertexVector.nor();
            double dot1 = Math.abs(segmentVector.dot(vertexVector));
            vertexVector = new VectorD(endPos.x - face.v2.x, endPos.y - face.v2.y, endPos.z - face.v2.z);
            vertexVector.nor();
            double dot2 = Math.abs(segmentVector.dot(vertexVector));
            vertexVector = new VectorD(endPos.x - face.v3.x, endPos.y - face.v3.y, endPos.z - face.v3.z);
            vertexVector.nor();
            double dot3 = Math.abs(segmentVector.dot(vertexVector));
            if (dot1 > dot2 && dot1 > dot3) {
                linedVertex = 1;
                linedVertexPos = face.v1.getPosition();
            } else if (dot2 > dot3 && dot2 > dot1) {
                linedVertex = 2;
                linedVertexPos = face.v2.getPosition();
            } else {
                linedVertex = 3;
                linedVertexPos = face.v3.getPosition();
            }

            // Now find which of the intersection endpoints is nearest to that vertex.
            if (linedVertexPos.dst(startPos) > linedVertexPos.dst(endPos)) {
                breakFaceInFive(facePos, startPos, endPos, linedVertex);
            } else {
                breakFaceInFive(facePos, endPos, startPos, linedVertex);
            }
        }
    }

    /**
     * Face breaker for VERTEX-EDGE-EDGE / EDGE-EDGE-VERTEX
     *
     * @param facePos   face position on the faces array
     * @param newPos    new vertex position
     * @param splitEdge that will be split
     */
    private void breakFaceInTwo(int facePos, VectorD newPos, int splitEdge) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex = addVertex(newPos, face, Vertex.BOUNDARY);

        if (splitEdge == 1) {
            addFace(face.v1, vertex, face.v3);
            addFace(vertex, face.v2, face.v3);
        } else if (splitEdge == 2) {
            addFace(face.v2, vertex, face.v1);
            addFace(vertex, face.v3, face.v1);
        } else {
            addFace(face.v3, vertex, face.v2);
            addFace(vertex, face.v1, face.v2);
        }
    }

    /**
     * Face breaker for VERTEX-FACE-EDGE / EDGE-FACE-VERTEX
     *
     * @param facePos   face position on the faces array
     * @param newPos    new vertex position
     * @param endVertex vertex used for splitting
     */
    private void breakFaceInTwo(int facePos, VectorD newPos, Vertex endVertex) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex = addVertex(newPos, face, Vertex.BOUNDARY);

        if (endVertex.equals(face.v1)) {
            addFace(face.v1, vertex, face.v3);
            addFace(vertex, face.v2, face.v3);
        } else if (endVertex.equals(face.v2)) {
            addFace(face.v2, vertex, face.v1);
            addFace(vertex, face.v3, face.v1);
        } else {
            addFace(face.v3, vertex, face.v2);
            addFace(vertex, face.v1, face.v2);
        }
    }

    /**
     * Face breaker for EDGE-EDGE-EDGE
     *
     * @param facePos   face position on the faces array
     * @param newPos1   new vertex position
     * @param newPos2   new vertex position
     * @param splitEdge edge that will be split
     */
    private void breakFaceInThree(int facePos, VectorD newPos1, VectorD newPos2, int splitEdge) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex1 = addVertex(newPos1, face, Vertex.BOUNDARY);
        Vertex vertex2 = addVertex(newPos2, face, Vertex.BOUNDARY);

        if (splitEdge == 1) {
            addFace(face.v1, vertex1, face.v3);
            addFace(vertex1, vertex2, face.v3);
            addFace(vertex2, face.v2, face.v3);
        } else if (splitEdge == 2) {
            addFace(face.v2, vertex1, face.v1);
            addFace(vertex1, vertex2, face.v1);
            addFace(vertex2, face.v3, face.v1);
        } else {
            addFace(face.v3, vertex1, face.v2);
            addFace(vertex1, vertex2, face.v2);
            addFace(vertex2, face.v1, face.v2);
        }
    }

    /**
     * Face breaker for VERTEX-FACE-FACE / FACE-FACE-VERTEX
     *
     * @param facePos   face position on the faces array
     * @param newPos    new vertex position
     * @param endVertex vertex used for the split
     */
    private void breakFaceInThree(int facePos, VectorD newPos, Vertex endVertex) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex = addVertex(newPos, face, Vertex.BOUNDARY);

        if (endVertex.equals(face.v1)) {
            addFace(face.v1, face.v2, vertex);
            addFace(face.v2, face.v3, vertex);
            addFace(face.v3, face.v1, vertex);
        } else if (endVertex.equals(face.v2)) {
            addFace(face.v2, face.v3, vertex);
            addFace(face.v3, face.v1, vertex);
            addFace(face.v1, face.v2, vertex);
        } else {
            addFace(face.v3, face.v1, vertex);
            addFace(face.v1, face.v2, vertex);
            addFace(face.v2, face.v3, vertex);
        }
    }

    /**
     * Face breaker for EDGE-FACE-EDGE
     *
     * @param facePos     face position on the faces array
     * @param newPos1     new vertex position
     * @param newPos2     new vertex position
     * @param startVertex vertex used the new faces creation
     * @param endVertex   vertex used for the new faces creation
     */
    private void breakFaceInThree(int facePos, VectorD newPos1, VectorD newPos2, Vertex startVertex, Vertex endVertex) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex1 = addVertex(newPos1, face, Vertex.BOUNDARY);
        Vertex vertex2 = addVertex(newPos2, face, Vertex.BOUNDARY);

        if (startVertex.equals(face.v1) && endVertex.equals(face.v2)) {
            addFace(face.v1, vertex1, vertex2);
            addFace(face.v1, vertex2, face.v3);
            addFace(vertex1, face.v2, vertex2);
        } else if (startVertex.equals(face.v2) && endVertex.equals(face.v1)) {
            addFace(face.v1, vertex2, vertex1);
            addFace(face.v1, vertex1, face.v3);
            addFace(vertex2, face.v2, vertex1);
        } else if (startVertex.equals(face.v2) && endVertex.equals(face.v3)) {
            addFace(face.v2, vertex1, vertex2);
            addFace(face.v2, vertex2, face.v1);
            addFace(vertex1, face.v3, vertex2);
        } else if (startVertex.equals(face.v3) && endVertex.equals(face.v2)) {
            addFace(face.v2, vertex2, vertex1);
            addFace(face.v2, vertex1, face.v1);
            addFace(vertex2, face.v3, vertex1);
        } else if (startVertex.equals(face.v3) && endVertex.equals(face.v1)) {
            addFace(face.v3, vertex1, vertex2);
            addFace(face.v3, vertex2, face.v2);
            addFace(vertex1, face.v1, vertex2);
        } else {
            addFace(face.v3, vertex2, vertex1);
            addFace(face.v3, vertex1, face.v2);
            addFace(vertex2, face.v1, vertex1);
        }
    }

    /**
     * Face breaker for FACE-FACE-FACE (a point only)
     *
     * @param facePos face position on the faces array
     * @param newPos  new vertex position
     */
    private void breakFaceInThree(int facePos, VectorD newPos) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex = addVertex(newPos, face, Vertex.BOUNDARY);

        addFace(face.v1, face.v2, vertex);
        addFace(face.v2, face.v3, vertex);
        addFace(face.v3, face.v1, vertex);
    }

    /**
     * Face breaker for EDGE-FACE-FACE / FACE-FACE-EDGE
     *
     * @param facePos   face position on the faces array
     * @param newPos1   new vertex position
     * @param newPos2   new vertex position
     * @param endVertex vertex used for the split
     */
    private void breakFaceInFour(int facePos, VectorD newPos1, VectorD newPos2, Vertex endVertex) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex1 = addVertex(newPos1, face, Vertex.BOUNDARY);
        Vertex vertex2 = addVertex(newPos2, face, Vertex.BOUNDARY);

        if (endVertex.equals(face.v1)) {
            addFace(face.v1, vertex1, vertex2);
            addFace(vertex1, face.v2, vertex2);
            addFace(face.v2, face.v3, vertex2);
            addFace(face.v3, face.v1, vertex2);
        } else if (endVertex.equals(face.v2)) {
            addFace(face.v2, vertex1, vertex2);
            addFace(vertex1, face.v3, vertex2);
            addFace(face.v3, face.v1, vertex2);
            addFace(face.v1, face.v2, vertex2);
        } else {
            addFace(face.v3, vertex1, vertex2);
            addFace(vertex1, face.v1, vertex2);
            addFace(face.v1, face.v2, vertex2);
            addFace(face.v2, face.v3, vertex2);
        }
    }

    /**
     * Face breaker for FACE-FACE-FACE
     *
     * @param facePos     face position on the faces array
     * @param newPos1     new vertex position
     * @param newPos2     new vertex position
     * @param linedVertex what vertex is more lined with the interersection found
     */
    private void breakFaceInFive(int facePos, VectorD newPos1, VectorD newPos2, int linedVertex) {
        Face face = faces.get(facePos);
        faces.remove(facePos);

        Vertex vertex1 = addVertex(newPos1, face, Vertex.BOUNDARY);
        Vertex vertex2 = addVertex(newPos2, face, Vertex.BOUNDARY);

        double cont = 0;
        if (linedVertex == 1) {
            addFace(face.v2, face.v3, vertex1);
            addFace(face.v2, vertex1, vertex2);
            addFace(face.v3, vertex2, vertex1);
            addFace(face.v2, vertex2, face.v1);
            addFace(face.v3, face.v1, vertex2);
        } else if (linedVertex == 2) {
            addFace(face.v3, face.v1, vertex1);
            addFace(face.v3, vertex1, vertex2);
            addFace(face.v1, vertex2, vertex1);
            addFace(face.v3, vertex2, face.v2);
            addFace(face.v1, face.v2, vertex2);
        } else {
            addFace(face.v1, face.v2, vertex1);
            addFace(face.v1, vertex1, vertex2);
            addFace(face.v2, vertex2, vertex1);
            addFace(face.v1, vertex2, face.v3);
            addFace(face.v2, face.v3, vertex2);
        }
    }

    //-----------------------------------OTHERS-------------------------------------//

    /**
     * Classify faces as being inside, outside or on boundary of other object
     *
     * @param solid solid used for the comparison
     */
    public boolean classifyFaces(Solid solid) throws BooleanOperationException {
        // calculate adjacency information
        Face face;
        for (int i = 0; i < this.getNumFaces(); i++) {
            face = this.getFace(i);
            face.v1.addAdjacentVertex(face.v2);
            face.v1.addAdjacentVertex(face.v3);
            face.v2.addAdjacentVertex(face.v1);
            face.v2.addAdjacentVertex(face.v3);
            face.v3.addAdjacentVertex(face.v1);
            face.v3.addAdjacentVertex(face.v2);
        }

        // for each face
        for (int i = 0; i < getNumFaces(); i++) {
            face = getFace(i);

            // if the face vertices aren't classified to make the simple classify
            if (!face.simpleClassify()) {
                // makes the ray trace classification
                face.rayTraceClassify(solid); // 可能死循环

                // mark the vertices
                if (face.v1.getStatus() == Vertex.UNKNOWN) {
                    face.v1.mark(face.getStatus());
                }
                if (face.v2.getStatus() == Vertex.UNKNOWN) {
                    face.v2.mark(face.getStatus());
                }
                if (face.v3.getStatus() == Vertex.UNKNOWN) {
                    face.v3.mark(face.getStatus());
                }
            }
        }
        return true;
    }

    /**
     * Inverts faces classified as INSIDE, making its normals point outside. Usually
     * used into the second solid when the difference is applied.
     */
    public void invertInsideFaces() {
        Face face;
        for (int i = 0; i < getNumFaces(); i++) {
            face = getFace(i);
            if (face.getStatus() == Face.INSIDE) {
                face.invert();
            }
        }
    }
}
