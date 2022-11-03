package com.my.world.enhanced.bool.operation;

import com.my.world.core.util.Disposable;
import com.my.world.enhanced.bool.util.EnhancedPool;
import com.my.world.enhanced.bool.util.NumberUtil;
import lombok.var;

/**
 * Representation of a 3d line or a ray (represented by a direction and a point).
 *
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Line implements Cloneable, Disposable {
    /**
     * a line point
     */
    private VectorD point;
    /**
     * line direction
     */
    private VectorD direction;

    //----------------------------------CONSTRUCTORS---------------------------------//

    public static final EnhancedPool<Line> pool = new EnhancedPool<>(Line::new);

    public static Line obtain(VectorD direction, VectorD point) {
        var obtain = pool.obtain();
        obtain.direction = (VectorD) direction.copy();
        obtain.point = (VectorD) point.copy();
        obtain.direction.nor();
        return obtain;
    }

    public static Line obtain(Face face1, Face face2) {
        var obtain = pool.obtain();
        VectorD normalFace1 = face1.getNormal();
        VectorD normalFace2 = face2.getNormal();

        // direction: cross product of the faces normals
        obtain.direction = VectorD.obtain();
        obtain.direction.cross(normalFace1, normalFace2);

        // if direction lenght is not zero (the planes aren't parallel )...
        if (!(obtain.direction.len() < NumberUtil.dTOL)) {
            // getting a line point, zero is set to a coordinate whose direction
            // component isn't zero (line intersecting its origin plan)
            obtain.point = VectorD.obtain();
            double d1 = -(normalFace1.x * face1.v1.x + normalFace1.y * face1.v1.y + normalFace1.z * face1.v1.z);
            double d2 = -(normalFace2.x * face2.v1.x + normalFace2.y * face2.v1.y + normalFace2.z * face2.v1.z);
            if (Math.abs(obtain.direction.x) > NumberUtil.dTOL) {
                obtain.point.x = 0;
                obtain.point.y = (d2 * normalFace1.z - d1 * normalFace2.z) / obtain.direction.x;
                obtain.point.z = (d1 * normalFace2.y - d2 * normalFace1.y) / obtain.direction.x;
            } else if (Math.abs(obtain.direction.y) > NumberUtil.dTOL) {
                obtain.point.x = (d1 * normalFace2.z - d2 * normalFace1.z) / obtain.direction.y;
                obtain.point.y = 0;
                obtain.point.z = (d2 * normalFace1.x - d1 * normalFace2.x) / obtain.direction.y;
            } else {
                obtain.point.x = (d2 * normalFace1.y - d1 * normalFace2.y) / obtain.direction.z;
                obtain.point.y = (d1 * normalFace2.x - d2 * normalFace1.x) / obtain.direction.z;
                obtain.point.z = 0;
            }
        }

        obtain.direction.nor();
        return obtain;
    }

    //---------------------------------OVERRIDES------------------------------------//

    /**
     * Clones the Line object
     *
     * @return cloned Line object
     */
    public Object clone() {
        Line clone = pool.obtain();;
        clone.direction = (VectorD) direction.copy();
        clone.point = (VectorD) point.copy();
        return clone;
    }

    /**
     * Makes a string definition for the Line object
     *
     * @return the string definition
     */
    public String toString() {
        return "Direction: " + direction.toString() + "\nPoint: " + point.toString();
    }

    //-----------------------------------GETS---------------------------------------//

    /**
     * Gets the point used to represent the line
     *
     * @return point used to represent the line
     */
    public VectorD getPoint() {
        return (VectorD) point.copy();
    }

    /**
     * Sets a new point
     *
     * @param point new point
     */
    public void setPoint(VectorD point) {
        this.point = (VectorD) point.copy();
    }

    //-----------------------------------SETS---------------------------------------//

    /**
     * Gets the line direction
     *
     * @return line direction
     */
    public VectorD getDirection() {
        return (VectorD) direction.copy();
    }

    /**
     * Sets a new direction
     *
     * @param direction new direction
     */
    public void setDirection(VectorD direction) {
        this.direction = (VectorD) direction.copy();
    }

    //--------------------------------OTHERS----------------------------------------//

    /**
     * Computes the distance from the line point to another point
     *
     * @param otherPoint the point to compute the distance from the line point. The point
     *                   is supposed to be on the same line.
     * @return points distance. If the point submitted is behind the direction, the
     * distance is negative
     */
    public double computePointToPointDistance(VectorD otherPoint) {
        double distance = otherPoint.dst(point);
        VectorD vec = VectorD.obtain().set(otherPoint.x - point.x, otherPoint.y - point.y, otherPoint.z - point.z);
        vec.nor();
        if (vec.dot(direction) < 0) {
            return -distance;
        } else {
            return distance;
        }
    }

    /**
     * Computes the point resulting from the intersection with another line
     *
     * @param otherLine the other line to apply the intersection. The lines are supposed
     *                  to intersect
     * @return point resulting from the intersection. If the point coundn't be obtained, return null
     */
    public VectorD computeLineIntersection(Line otherLine) {
        // x = x1 + a1*t = x2 + b1*s
        // y = y1 + a2*t = y2 + b2*s
        // z = z1 + a3*t = z2 + b3*s

        VectorD linePoint = otherLine.getPoint();
        VectorD lineDirection = otherLine.getDirection();

        double t;
        if (Math.abs(direction.y * lineDirection.x - direction.x * lineDirection.y) > NumberUtil.dTOL) {
            t = (-point.y * lineDirection.x + linePoint.y * lineDirection.x + lineDirection.y * point.x - lineDirection.y * linePoint.x) / (direction.y * lineDirection.x - direction.x * lineDirection.y);
        } else if (Math.abs(-direction.x * lineDirection.z + direction.z * lineDirection.x) > NumberUtil.dTOL) {
            t = -(-lineDirection.z * point.x + lineDirection.z * linePoint.x + lineDirection.x * point.z - lineDirection.x * linePoint.z) / (-direction.x * lineDirection.z + direction.z * lineDirection.x);
        } else if (Math.abs(-direction.z * lineDirection.y + direction.y * lineDirection.z) > NumberUtil.dTOL) {
            t = (point.z * lineDirection.y - linePoint.z * lineDirection.y - lineDirection.z * point.y + lineDirection.z * linePoint.y) / (-direction.z * lineDirection.y + direction.y * lineDirection.z);
        } else return null;

        double x = point.x + direction.x * t;
        double y = point.y + direction.y * t;
        double z = point.z + direction.z * t;

        return VectorD.obtain().set(x, y, z);
    }

    /**
     * Compute the point resulting from the intersection with a plane
     *
     * @param normal     the plane normal
     * @param planePoint a plane point.
     * @return intersection point. If they don't intersect, return null
     */
    public VectorD computePlaneIntersection(VectorD normal, VectorD planePoint) {
        // Ax + By + Cz + D = 0
        // x = x0 + t(x1 � x0)
        // y = y0 + t(y1 � y0)
        // z = z0 + t(z1 � z0)
        // (x1 - x0) = dx, (y1 - y0) = dy, (z1 - z0) = dz
        // t = -(A*x0 + B*y0 + C*z0 )/(A*dx + B*dy + C*dz)

        double A = normal.x;
        double B = normal.y;
        double C = normal.z;
        double D = -(normal.x * planePoint.x + normal.y * planePoint.y + normal.z * planePoint.z);

        double numerator = A * point.x + B * point.y + C * point.z + D;
        double denominator = A * direction.x + B * direction.y + C * direction.z;

        // if line is paralel to the plane...
        if (Math.abs(denominator) < NumberUtil.dTOL) {
            // if line is contained in the plane...
            if (Math.abs(numerator) < NumberUtil.dTOL) {
                return (VectorD) point.copy();
            } else {
                return null;
            }
        }
        // if line intercepts the plane...
        else {
            double t = -numerator / denominator;
            VectorD resultPoint = VectorD.obtain();
            resultPoint.x = point.x + t * direction.x;
            resultPoint.y = point.y + t * direction.y;
            resultPoint.z = point.z + t * direction.z;

            return resultPoint;
        }
    }

    /**
     * Changes slightly the line direction
     */
    public void perturbDirection() {
        direction.x += 1e-5 * Math.random();
        direction.y += 1e-5 * Math.random();
        direction.z += 1e-5 * Math.random();
    }

    @Override
    public void dispose() {
        this.point = null;
        this.direction = null;
    }
}
