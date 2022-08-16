package com.my.demo.entity.house.room.blueprint;

import com.badlogic.gdx.math.Vector2;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Line {

    public final Vector2 a = new Vector2();
    public final Vector2 b = new Vector2();
    public String tag;

    private static final Vector2 tmpV = new Vector2();

    public Line(Vector2 a, Vector2 b) {
        this.a.set(a);
        this.b.set(b);
    }

    public float len() {
        return a.dst(b);
    }

    public float angleDeg() {
        tmpV.set(b).sub(a);
        return tmpV.angleDeg();
    }
}
