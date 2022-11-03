package com.my.world.enhanced.bool.operation;

import com.my.world.core.util.Disposable;
import com.my.world.enhanced.bool.util.EnhancedPool;
import com.my.world.enhanced.bool.util.LoggerUtil;
import com.my.world.enhanced.bool.util.NumberUtil;
import lombok.var;

/**
 * Representation of a 3D face (triangle).
 *
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Face implements Cloneable, Disposable {
    /**
     * face status if it is still unknown
     */
    public static final int UNKNOWN = 1;
    /**
     * face status if it is inside a solid
     */
    public static final int INSIDE = 2;
    /**
     * face status if it is outside a solid
     */
    public static final int OUTSIDE = 3;
    /**
     * face status if it is coincident with a solid face
     */
    public static final int SAME = 4;
    /**
     * face status if it is coincident with a solid face with opposite orientation
     */
    public static final int OPPOSITE = 5;
    /**
     * point status if it is up relative to an edge - see linePositionIn_ methods
     */
    private static final int UP = 6;
    /**
     * point status if it is down relative to an edge - see linePositionIn_ methods
     */
    private static final int DOWN = 7;
    /**
     * point status if it is on an edge - see linePositionIn_ methods
     */
    private static final int ON = 8;
    /**
     * point status if it isn't up, down or on relative to an edge - see linePositionIn_ methods
     */
    private static final int NONE = 9;
    /**
     * first vertex
     */
    public Vertex v1;
    /**
     * second vertex
     */
    public Vertex v2;
    /**
     * third vertex
     */
    public Vertex v3;
    /**
     * face status relative to a solid
     */
    private int status;

    //---------------------------------CONSTRUCTORS---------------------------------//

    public static final EnhancedPool<Face> pool = new EnhancedPool<>(Face::new);

    public static Face obtain(Vertex v1, Vertex v2, Vertex v3) {
        var obtain = pool.obtain();
        obtain.v1 = v1;
        obtain.v2 = v2;
        obtain.v3 = v3;
        obtain.status = UNKNOWN;
        return obtain;
    }

    //-----------------------------------OVERRIDES----------------------------------//

    /**
     * Gets the position of a point relative to a line in the x plane
     *
     * @param point      point to be tested
     * @param pointLine1 one of the line ends
     * @param pointLine2 one of the line ends
     * @return position of the point relative to the line - UP, DOWN, ON, NONE
     */
    private static int linePositionInX(VectorD point, VectorD pointLine1, VectorD pointLine2) {
        double a, b, z;
        if ((Math.abs(pointLine1.y - pointLine2.y) > NumberUtil.dTOL) && (((point.y >= pointLine1.y) && (point.y <= pointLine2.y)) || ((point.y <= pointLine1.y) && (point.y >= pointLine2.y)))) {
            a = (pointLine2.z - pointLine1.z) / (pointLine2.y - pointLine1.y);
            b = pointLine1.z - a * pointLine1.y;
            z = a * point.y + b;
            if (z > point.z + NumberUtil.dTOL) {
                return UP;
            } else if (z < point.z - NumberUtil.dTOL) {
                return DOWN;
            } else {
                return ON;
            }
        } else {
            return NONE;
        }
    }

    /**
     * Gets the position of a point relative to a line in the y plane
     *
     * @param point      point to be tested
     * @param pointLine1 one of the line ends
     * @param pointLine2 one of the line ends
     * @return position of the point relative to the line - UP, DOWN, ON, NONE
     */

    private static int linePositionInY(VectorD point, VectorD pointLine1, VectorD pointLine2) {
        double a, b, z;
        if ((Math.abs(pointLine1.x - pointLine2.x) > NumberUtil.dTOL) && (((point.x >= pointLine1.x) && (point.x <= pointLine2.x)) || ((point.x <= pointLine1.x) && (point.x >= pointLine2.x)))) {
            a = (pointLine2.z - pointLine1.z) / (pointLine2.x - pointLine1.x);
            b = pointLine1.z - a * pointLine1.x;
            z = a * point.x + b;
            if (z > point.z + NumberUtil.dTOL) {
                return UP;
            } else if (z < point.z - NumberUtil.dTOL) {
                return DOWN;
            } else {
                return ON;
            }
        } else {
            return NONE;
        }
    }

    /**
     * Gets the position of a point relative to a line in the z plane
     *
     * @param point      point to be tested
     * @param pointLine1 one of the line ends
     * @param pointLine2 one of the line ends
     * @return position of the point relative to the line - UP, DOWN, ON, NONE
     */

    private static int linePositionInZ(VectorD point, VectorD pointLine1, VectorD pointLine2) {
        double a, b, y;
        if ((Math.abs(pointLine1.x - pointLine2.x) > NumberUtil.dTOL) && (((point.x >= pointLine1.x) && (point.x <= pointLine2.x)) || ((point.x <= pointLine1.x) && (point.x >= pointLine2.x)))) {
            a = (pointLine2.y - pointLine1.y) / (pointLine2.x - pointLine1.x);
            b = pointLine1.y - a * pointLine1.x;
            y = a * point.x + b;
            if (y > point.y + NumberUtil.dTOL) {
                return UP;
            } else if (y < point.y - NumberUtil.dTOL) {
                return DOWN;
            } else {
                return ON;
            }
        } else {
            return NONE;
        }
    }

    //-------------------------------------GETS-------------------------------------//

    /**
     * Clones the face object
     *
     * @return cloned face object
     */
    public Object clone() {
        Face clone = pool.obtain();;
        clone.v1 = (Vertex) v1.clone();
        clone.v2 = (Vertex) v2.clone();
        clone.v3 = (Vertex) v3.clone();
        clone.status = status;
        return clone;
    }

    /**
     * Makes a string definition for the Face object
     *
     * @return the string definition
     */
    public String toString() {
        return v1.toString() + "\n" + v2.toString() + "\n" + v3.toString();
    }

    /**
     * Checks if a face is equal to another. To be equal, they have to have equal
     * vertices in the same order
     *
     * @param anObject the other face to be tested
     * @return true if they are equal, false otherwise.
     */
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Face)) {
            return false;
        } else {
            Face face = (Face) anObject;
            boolean cond1 = v1.equals(face.v1) && v2.equals(face.v2) && v3.equals(face.v3);
            boolean cond2 = v1.equals(face.v2) && v2.equals(face.v3) && v3.equals(face.v1);
            boolean cond3 = v1.equals(face.v3) && v2.equals(face.v1) && v3.equals(face.v2);

            return cond1 || cond2 || cond3;
        }
    }

    /**
     * Gets the face bound
     *
     * @return face bound
     */
    public Bound getBound() {
        return Bound.obtain(v1.getPosition(), v2.getPosition(), v3.getPosition());
    }

    //-------------------------------------OTHERS-----------------------------------//

    /**
     * Gets the face normal
     *
     * @return face normal
     */
    public VectorD getNormal() {
        VectorD p1 = v1.getPosition();
        VectorD p2 = v2.getPosition();
        VectorD p3 = v3.getPosition();
        VectorD xy, xz, normal;

        xy = VectorD.obtain().set(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
        xz = VectorD.obtain().set(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);

        normal = VectorD.obtain();
        normal.cross(xy, xz);
        normal.nor();

        return normal;
    }

    //------------------------------------CLASSIFIERS-------------------------------//

    /**
     * Gets the face status
     *
     * @return face status - UNKNOWN, INSIDE, OUTSIDE, SAME OR OPPOSITE
     */
    public int getStatus() {
        return status;
    }

    /**
     * Gets the face area
     *
     * @return face area
     */
    public double getArea() {
        // area = (a * c * sen(B))/2
        VectorD p1 = v1.getPosition();
        VectorD p2 = v2.getPosition();
        VectorD p3 = v3.getPosition();
        VectorD xy = VectorD.obtain().set(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
        VectorD xz = VectorD.obtain().set(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);

        double a = p1.dst(p2);
        double c = p1.dst(p3);
        double B = xy.angle(xz);

        return (a * c * Math.sin(B)) / 2d;
    }

    //------------------------------------PRIVATES----------------------------------//

    /**
     * Invert face direction (normal direction)
     */
    public void invert() {
        Vertex vertexTemp = v2;
        v2 = v1;
        v1 = vertexTemp;
    }

    /**
     * Classifies the face if one of its vertices are classified as INSIDE or OUTSIDE
     *
     * @return true if the face could be classified, false otherwise
     */
    public boolean simpleClassify() {
        int status1 = v1.getStatus();
        int status2 = v2.getStatus();
        int status3 = v3.getStatus();

        if (status1 == Vertex.INSIDE || status1 == Vertex.OUTSIDE) {
            this.status = status1;
            return true;
        } else if (status2 == Vertex.INSIDE || status2 == Vertex.OUTSIDE) {
            this.status = status2;
            return true;
        } else if (status3 == Vertex.INSIDE || status3 == Vertex.OUTSIDE) {
            this.status = status3;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Classifies the face based on the ray trace technique
     *
     * @param solid solid used to compute the face status
     */
    public void rayTraceClassify(Solid solid) throws BooleanOperationException {
        // creating a ray starting starting at the face baricenter going to the normal direction
        VectorD p0 = VectorD.obtain();
        p0.x = (v1.x + v2.x + v3.x) / 3d;
        p0.y = (v1.y + v2.y + v3.y) / 3d;
        p0.z = (v1.z + v2.z + v3.z) / 3d;
        Line ray = Line.obtain(getNormal(), p0);

        boolean success;
        double dotProduct, distance;
        VectorD intersectionPoint;
        Face closestFace = null;
        double closestDistance;
        int repeat = 0;  // 防止死循环

        do {
            success = true;
            closestDistance = Double.MAX_VALUE;
            // for each face from the other solid...
            for (int i = 0; i < solid.getNumFaces(); i++) {
                Face face = solid.getFace(i);
                dotProduct = face.getNormal().dot(ray.getDirection());
                intersectionPoint = ray.computePlaneIntersection(face.getNormal(), face.v1.getPosition());

                // if ray intersects the plane...
                if (intersectionPoint != null) {
                    distance = ray.computePointToPointDistance(intersectionPoint);

                    // if ray lies in plane...
                    if (Math.abs(distance) < NumberUtil.dTOL && Math.abs(dotProduct) < NumberUtil.dTOL) {
                        if (repeat > 50000) {  // TODO: 以重复多少次为失败标准
                            LoggerUtil.log(2, "possible infinite loop situation: terminating rayTraceClassify - Too Many Repeat");
                            throw new BooleanOperationException("possible infinite loop situation: terminating rayTraceClassify - Too Many Repeat");
                        }
                        repeat++;
                        // disturb the ray in order to not lie into another plane
                        ray.perturbDirection();
                        success = false;
                        break;
                    }

                    // if ray starts in plane...
                    if (Math.abs(distance) < NumberUtil.dTOL && Math.abs(dotProduct) > NumberUtil.dTOL) {
                        // if ray intersects the face...
                        if (face.hasPoint(intersectionPoint)) {
                            // faces coincide
                            closestFace = face;
                            closestDistance = 0;
                            break;
                        }
                    }

                    // if ray intersects plane...
                    else if (Math.abs(dotProduct) > NumberUtil.dTOL && distance > NumberUtil.dTOL) {
                        if (distance < closestDistance) {
                            // if ray intersects the face;
                            if (face.hasPoint(intersectionPoint)) {
                                // this face is the closest face untill now
                                closestDistance = distance;
                                closestFace = face;
                            }
                        }
                    }
                }
            }
        } while (!success); // 添加变量repeat以防止死循环

        // none face found: outside face
        if (closestFace == null) {
            status = OUTSIDE;
        }
        // face found: test dot product
        else {
            dotProduct = closestFace.getNormal().dot(ray.getDirection());

            // distance = 0: coplanar faces
            if (Math.abs(closestDistance) < NumberUtil.dTOL) {
                if (dotProduct > NumberUtil.dTOL) {
                    status = SAME;
                } else if (dotProduct < -NumberUtil.dTOL) {
                    status = OPPOSITE;
                }
            }

            // dot product > 0 (same direction): inside face
            else if (dotProduct > NumberUtil.dTOL) {
                status = INSIDE;
            }

            // dot product < 0 (opposite direction): outside face
            else if (dotProduct < -NumberUtil.dTOL) {
                status = OUTSIDE;
            }
        }
    }

    /**
     * Checks if the the face contains a point
     *
     * @param point to be tested
     * @return true if the face contains the point, false otherwise
     */
    private boolean hasPoint(VectorD point) {
        int result1, result2, result3;
        boolean hasUp, hasDown, hasOn;
        VectorD normal = getNormal();

        // if x is constant...
        if (Math.abs(normal.x) > NumberUtil.dTOL) {
            // tests on the x plane
            result1 = linePositionInX(point, v1.getPosition(), v2.getPosition());
            result2 = linePositionInX(point, v2.getPosition(), v3.getPosition());
            result3 = linePositionInX(point, v3.getPosition(), v1.getPosition());
        }

        // if y is constant...
        else if (Math.abs(normal.y) > NumberUtil.dTOL) {
            // tests on the y plane
            result1 = linePositionInY(point, v1.getPosition(), v2.getPosition());
            result2 = linePositionInY(point, v2.getPosition(), v3.getPosition());
            result3 = linePositionInY(point, v3.getPosition(), v1.getPosition());
        } else {
            // tests on the z plane
            result1 = linePositionInZ(point, v1.getPosition(), v2.getPosition());
            result2 = linePositionInZ(point, v2.getPosition(), v3.getPosition());
            result3 = linePositionInZ(point, v3.getPosition(), v1.getPosition());
        }

        // if the point is up and down two lines...
        if (((result1 == UP) || (result2 == UP) || (result3 == UP)) && ((result1 == DOWN) || (result2 == DOWN) || (result3 == DOWN))) {
            return true;
        }
        // if the point is on of the lines...
        else return (result1 == ON) || (result2 == ON) || (result3 == ON);
    }

    @Override
    public void dispose() {
        this.v1 = null;
        this.v2 = null;
        this.v3 = null;
        this.status = UNKNOWN;
    }
}
