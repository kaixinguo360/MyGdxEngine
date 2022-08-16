package com.my.demo.entity.house.room.blueprint;

import com.badlogic.gdx.math.Vector2;

public class Point {

    public Vector2 center = new Vector2();
    public Vector2 direction = new Vector2();
    public String tag;

    public Point(Vector2 center) {
        this.center.set(center);
        this.direction.setZero();
    }

    public Point(Vector2 center, Vector2 direction) {
        this.center.set(center);
        this.direction.set(direction);
    }

    public float angleDeg() {
        return direction.angleDeg();
    }
}
