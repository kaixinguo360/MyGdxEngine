package com.my.world.enhanced.bool.operation;

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
public class Bound {
    /**
     * tolerance value to test equalities
     */
    private static final double TOL = 1e-10f;
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

    /**
     * Bound constructor for a face
     *
     * @param p1 point relative to the first vertex
     * @param p2 point relative to the second vertex
     * @param p3 point relative to the third vertex
     */
    public Bound(VectorD p1, VectorD p2, VectorD p3) {
        xMax = xMin = p1.x;
        yMax = yMin = p1.y;
        zMax = zMin = p1.z;

        checkVertex(p2);
        checkVertex(p3);
    }

    /**
     * Bound constructor for a object 3d
     *
     * @param vertices the object vertices
     */
    public Bound(VectorD[] vertices) {
        xMax = xMin = vertices[0].x;
        yMax = yMin = vertices[0].y;
        zMax = zMin = vertices[0].z;

        for (int i = 1; i < vertices.length; i++) {
            checkVertex(vertices[i]);
        }
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
        return (!(xMin > bound.xMax + TOL)) && (!(xMax < bound.xMin - TOL)) && (!(yMin > bound.yMax + TOL)) && (!(yMax < bound.yMin - TOL)) && (!(zMin > bound.zMax + TOL)) && (!(zMax < bound.zMin - TOL));
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
