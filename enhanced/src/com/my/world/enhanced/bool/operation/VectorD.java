package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class VectorD {

    public double x;
    public double y;
    public double z;

    VectorD() {
    }

    VectorD(double x, double y, double z) {
        this.set(x, y, z);
    }

    VectorD(final VectorD vector) {
        this.set(vector);
    }

    VectorD(final Vector3 vector3) {
        this.set(vector3.x, vector3.y, vector3.z);
    }

    Vector3 toVector3(Vector3 vector3) {
        return vector3.set((float) x, (float) y, (float) z);
    }

    VectorD setFromVector3(Vector3 vector3) {
        set(vector3.x, vector3.y, vector3.z);
        return this;
    }

    VectorD set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    VectorD set(final VectorD vector) {
        return this.set(vector.x, vector.y, vector.z);
    }

    double dot(final VectorD vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    double distance(final VectorD vector) {
        final double a = vector.x - x;
        final double b = vector.y - y;
        final double c = vector.z - z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    double angle(VectorD vector) {
        double vDot = this.dot(vector) / (this.length() * vector.length());
        if (vDot < -1.0) vDot = -1.0f;
        if (vDot > 1.0) vDot = 1.0f;
        return Math.acos(vDot);
    }

    void normalize() {
        final double len2 = this.len2();
        if (len2 == 0f || len2 == 1f) return;
        this.scl(1f / Math.sqrt(len2));
    }

    void cross(final VectorD vector1, final VectorD vector2) {
        this.set(vector1);
        this.set(y * vector2.z - z * vector2.y, z * vector2.x - x * vector2.z, x * vector2.y - y * vector2.x);
    }

    private void scl(double scalar) {
        this.set(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    private double len2() {
        return x * x + y * y + z * z;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    @Override
    public Object clone() {
        return new VectorD(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        long result = 1;
        result = prime * result + NumberUtils.doubleToLongBits(x);
        result = prime * result + NumberUtils.doubleToLongBits(y);
        result = prime * result + NumberUtils.doubleToLongBits(z);
        return (int) result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VectorD other = (VectorD) obj;
        if (NumberUtils.doubleToLongBits(x) != NumberUtils.doubleToLongBits(other.x)) return false;
        if (NumberUtils.doubleToLongBits(y) != NumberUtils.doubleToLongBits(other.y)) return false;
        return NumberUtils.doubleToLongBits(z) == NumberUtils.doubleToLongBits(other.z);
    }
}
