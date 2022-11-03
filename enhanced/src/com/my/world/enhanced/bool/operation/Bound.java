package com.my.world.enhanced.bool.operation;

import com.my.world.core.util.Disposable;
import com.my.world.enhanced.bool.util.EnhancedPool;
import com.my.world.enhanced.bool.util.NumberUtil;

/**
 * Representation of a bound - the extremes of a 3d component for each coordinate.
 *
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Bound implements Disposable {

    /**
     * maximum from the x coordinate
     */
    double xMax;
    /**
     * minimum from the x coordinate
     */
    double xMin;
    /**
     * maximum from the y coordinate
     */
    double yMax;
    /**
     * minimum from the y coordinate
     */
    double yMin;
    /**
     * maximum from the z coordinate
     */
    double zMax;
    /**
     * minimum from the z coordinate
     */
    double zMin;

    //---------------------------------CONSTRUCTORS---------------------------------//

    public static final EnhancedPool<Bound> pool = new EnhancedPool<>(Bound::new);

    public static Bound obtain(VectorD[] vertices) {
        Bound obtain = pool.obtain();
        obtain.xMax = obtain.xMin = vertices[0].x;
        obtain.yMax = obtain.yMin = vertices[0].y;
        obtain.zMax = obtain.zMin = vertices[0].z;
        for (int i = 1; i < vertices.length; i++) {
            obtain.checkVertex(vertices[i]);
        }
        return obtain;
    }

    public static Bound obtain(VectorD p1, VectorD p2, VectorD p3) {
        Bound obtain = pool.obtain();
        obtain.xMax = obtain.xMin = p1.x;
        obtain.yMax = obtain.yMin = p1.y;
        obtain.zMax = obtain.zMin = p1.z;
        obtain.checkVertex(p2);
        obtain.checkVertex(p3);
        return obtain;
    }

    @Override
    public void dispose() {
        this.xMax = 0;
        this.xMin = 0;
        this.yMax = 0;
        this.yMin = 0;
        this.zMax = 0;
        this.zMin = 0;
    }

    //----------------------------------OVERRIDES-----------------------------------//

    /**
     * Makes a string definition for the bound object
     *
     * @return the string definition
     */
    public String toString() {
        return "x: " + xMin + " .. " + xMax + "\ny: " + yMin + " .. " + yMax + "\nz: " + zMin + " .. " + zMax;
    }

    //--------------------------------------OTHERS----------------------------------//

    /**
     * Checks if a bound overlaps other one
     *
     * @param bound other bound to make the comparison
     * @return true if they insersect, false otherwise
     */
    public boolean overlap(Bound bound) {
        return (!(xMin > bound.xMax + NumberUtil.dTOL)) && (!(xMax < bound.xMin - NumberUtil.dTOL)) && (!(yMin > bound.yMax + NumberUtil.dTOL)) && (!(yMax < bound.yMin - NumberUtil.dTOL)) && (!(zMin > bound.zMax + NumberUtil.dTOL)) && (!(zMax < bound.zMin - NumberUtil.dTOL));
    }

    //-------------------------------------PRIVATES---------------------------------//

    /**
     * Checks if one of the coordinates of a vertex exceed the ones found before
     *
     * @param vertex vertex to be tested
     */
    private void checkVertex(VectorD vertex) {
        if (vertex.x > xMax) {
            xMax = vertex.x;
        } else if (vertex.x < xMin) {
            xMin = vertex.x;
        }

        if (vertex.y > yMax) {
            yMax = vertex.y;
        } else if (vertex.y < yMin) {
            yMin = vertex.y;
        }

        if (vertex.z > zMax) {
            zMax = vertex.z;
        } else if (vertex.z < zMin) {
            zMin = vertex.z;
        }
    }
}
