package com.my.demo.entity.house.room.blueprint;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaneBlueprint {

    public final List<Vector2> vertexes = new ArrayList<>();
    public final List<Point> points = new ArrayList<>();
    public final List<Line> lines = new ArrayList<>();

    // ----- Config Methods ----- //

    protected void config() {}

    // ----- Draw Util Methods ----- //

    public Line line(String tag, Vector2 v1, Vector2 v2) {
        Line line = new Line(v1, v2);
        line.tag = tag;
        lines.add(line);
        return line;
    }

    public Point point(String tag, Vector2 center, Vector2 direction) {
        Point point = new Point(center, direction);
        point.tag = tag;
        points.add(point);
        return point;
    }

    public Point point(String tag, Vector2 center) {
        Point point = new Point(center);
        point.tag = tag;
        points.add(point);
        return point;
    }

    public Vector2 vertex(float x, float y) {
        Vector2 vertex = new Vector2(x, y);
        vertexes.add(vertex);
        return vertex;
    }

    // ----- Random Number Util Methods ----- //

    public static int randInt(int min, int max, Random random) {
        return Math.round(min + (max - min) * random.nextFloat());
    }

    public static long randLong(long min, long max, Random random) {
        return Math.round(min + (max - min) * random.nextDouble());
    }

    public static float randFloat(float min, float max, Random random) {
        return min + (max - min) * random.nextFloat();
    }

    public static double randDouble(double min, double max, Random random) {
        return min + (max - min) * random.nextDouble();
    }

    public static boolean randBool(float probability, Random random) {
        return random.nextFloat() <= probability;
    }

    public static boolean randBool(Random random) {
        return randBool(0.5f, random);
    }
}
