package com.my.demo.entity.house.room;

import com.badlogic.gdx.math.Vector2;
import com.my.demo.entity.house.room.blueprint.Line;
import com.my.demo.entity.house.room.blueprint.PlaneBlueprint;

import java.util.Random;

public class CorridorRoomBlueprint extends PlaneBlueprint {

    public static final String WALL = "WALL";
    public static final String DOOR = "DOOR";
    public static final String WINDOW = "WINDOW";
    public static final String PILLAR = "PILLAR";

    public long randomSeed = 10000L;
    public float roomWidth = 10f;
    public float roomLength = 10f;
    public float doorWidth = 1.5f;
    public float corridorWidth = 3f;
    public float roomMinLength = 3f;
    public float specialRoomThreshold1 = 10;
    public float specialRoomThreshold2 = 8;

    { config(); }

    public CorridorRoomBlueprint() {
        Random r = new Random(randomSeed);

        // Build exterior wall vertexes
        float wallX = roomWidth / 2;
        float wallY = roomLength / 2;
        Vector2 wallV00 = vertex(-wallX, -wallY);
        Vector2 wallV10 = vertex(wallX, -wallY);
        Vector2 wallV11 = vertex(wallX, wallY);
        Vector2 wallV01 = vertex(-wallX, wallY);

        // Build entrance vertexes
        float entranceX = doorWidth / 2;
        Vector2 entranceV1 = vertex(-entranceX, -wallY);
        Vector2 entranceV2 = vertex(entranceX, -wallY);

        // Build exterior wall pillars
        point(PILLAR, wallV00);
        point(PILLAR, wallV10);
        point(PILLAR, wallV11);
        point(PILLAR, wallV01);

        // Build exterior walls
        {
            Line wall1_part0 = line(WALL, wallV00, entranceV1);
            Line wall1_door = line(DOOR, entranceV1, entranceV2);
            Line wall1_part1 = line(WALL, entranceV2, wallV10);
            Line wall2 = line(WALL, wallV10, wallV11);
            Line wall3 = line(WALL, wallV11, wallV01);
            Line wall4 = line(WALL, wallV01, wallV00);
        }

        // Build corridor vertexes
        float corridorX = corridorWidth / 2;
        Vector2 v00 = vertex(-corridorX, -wallY);
        Vector2 v10 = vertex(corridorX, -wallY);

        // Get the number of rooms
        int roomNum = randInt(1, (int) (roomLength / roomMinLength),  r);
        {
            // Limit the number of rooms
            float roomLength = this.roomLength / roomNum;
            while (roomLength < roomMinLength && roomNum > 1) {
                roomNum--;
                roomLength = this.roomLength / roomNum;
            }
        }

        // Build rooms
        float[] roomYs = new float[roomNum];
        roomYs[0] = -wallY;
        for (int i = 1; i <= roomNum; i++) {
            float prevY = roomYs[i - 1];
            int remainNum = roomNum - i;
            if (remainNum == 0) {
                // Last Room
                float currentLength = wallY - prevY;
                System.out.println("Last Room:\t" + currentLength);
                float y1 = prevY + (randBool(r)
                        ? doorWidth / 2
                        : currentLength - doorWidth / 2 - doorWidth
                );
                float y2 = y1 + doorWidth;
                float y3 = prevY + currentLength;
                Vector2 v01 = vertex(-corridorX, y1);
                Vector2 v02 = vertex(-corridorX, y2);
                Vector2 v03 = vertex(-corridorX, y3);
                line(WALL, v00, v01);
                line(DOOR, v01, v02);
                line(WALL, v02, v03);
                Vector2 v11 = vertex(corridorX, y1);
                Vector2 v12 = vertex(corridorX, y2);
                Vector2 v13 = vertex(corridorX, y3);
                line(WALL, v10, v11);
                line(DOOR, v11, v12);
                line(WALL, v12, v13);
                v00 = v03;
                v10 = v13;
            } else {
                float remainLength = wallY - prevY;
                float availableMinLength = roomMinLength;
                float availableMaxLength = remainLength - roomMinLength * remainNum;
                float currentLength;
                if (availableMinLength >= availableMaxLength) {
                    // Small Room
                    currentLength = (availableMinLength + availableMaxLength) / 2;
                    System.out.println("Small Room:\t" + currentLength);
                    if (currentLength > doorWidth) {
                        float y1 = prevY + currentLength / 2 - doorWidth / 2;
                        float y2 = y1 + doorWidth;
                        float y3 = prevY + currentLength;
                        Vector2 v01 = vertex(-corridorX, y1);
                        Vector2 v02 = vertex(-corridorX, y2);
                        Vector2 v03 = vertex(-corridorX, y3);
                        line(WALL, v00, v01);
                        line(DOOR, v01, v02);
                        line(WALL, v02, v03);
                        Vector2 v11 = vertex(corridorX, y1);
                        Vector2 v12 = vertex(corridorX, y2);
                        Vector2 v13 = vertex(corridorX, y3);
                        line(WALL, v10, v11);
                        line(DOOR, v11, v12);
                        line(WALL, v12, v13);
                        v00 = v03;
                        v10 = v13;
                    }
                } else {
                    if (availableMaxLength >= specialRoomThreshold1 && randBool(0.2f, r)) {
                        // Two-Door Room
                        currentLength = randFloat(specialRoomThreshold1, availableMaxLength,  r);
                        System.out.println("Two-Door Room:\t" + currentLength);
                        float y1 = prevY + doorWidth / 2;
                        float y2 = y1 + doorWidth;
                        float y3 = prevY + currentLength - doorWidth / 2 - doorWidth;
                        float y4 = y3 + doorWidth;
                        float y5 = prevY + currentLength;
                        Vector2 v01 = vertex(-corridorX, y1);
                        Vector2 v02 = vertex(-corridorX, y2);
                        Vector2 v03 = vertex(-corridorX, y3);
                        Vector2 v04 = vertex(-corridorX, y4);
                        Vector2 v05 = vertex(-corridorX, y5);
                        line(WALL, v00, v01);
                        line(DOOR, v01, v02);
                        line(WALL, v02, v03);
                        line(DOOR, v03, v04);
                        line(WALL, v04, v05);
                        Vector2 v11 = vertex(corridorX, y1);
                        Vector2 v12 = vertex(corridorX, y2);
                        Vector2 v13 = vertex(corridorX, y3);
                        Vector2 v14 = vertex(corridorX, y4);
                        Vector2 v15 = vertex(corridorX, y5);
                        line(WALL, v10, v11);
                        line(DOOR, v11, v12);
                        line(WALL, v12, v13);
                        line(DOOR, v13, v14);
                        line(WALL, v14, v15);
                        v00 = v05;
                        v10 = v15;
                    } else if (availableMaxLength >= specialRoomThreshold2 && randBool(0.2f, r)) {
                        // Big-Door Room
                        currentLength = randFloat(specialRoomThreshold2, availableMaxLength,  r);
                        System.out.println("Big-Door Room:\t" + currentLength);
                        float y1 = prevY + currentLength / 2 - doorWidth / 2;
                        float y2 = y1 + doorWidth;
                        float y3 = prevY + currentLength;
                        Vector2 v01 = vertex(-corridorX, y1);
                        Vector2 v02 = vertex(-corridorX, y2);
                        Vector2 v03 = vertex(-corridorX, y3);
                        line(WALL, v00, v01);
                        line(DOOR, v01, v02);
                        line(WALL, v02, v03);
                        Vector2 v11 = vertex(corridorX, y1);
                        Vector2 v12 = vertex(corridorX, y2);
                        Vector2 v13 = vertex(corridorX, y3);
                        line(WALL, v10, v11);
                        line(DOOR, v11, v12);
                        line(WALL, v12, v13);
                        v00 = v03;
                        v10 = v13;
                    } else {
                        // Normal Room
                        currentLength = randFloat(availableMinLength, availableMaxLength,  r);
                        System.out.println("Normal Room:\t" + currentLength);
                        float y1 = prevY + (randBool(r)
                                ? doorWidth / 2
                                : currentLength - doorWidth / 2 - doorWidth
                        );
                        float y2 = y1 + doorWidth;
                        float y3 = prevY + currentLength;
                        Vector2 v01 = vertex(-corridorX, y1);
                        Vector2 v02 = vertex(-corridorX, y2);
                        Vector2 v03 = vertex(-corridorX, y3);
                        line(WALL, v00, v01);
                        line(DOOR, v01, v02);
                        line(WALL, v02, v03);
                        Vector2 v11 = vertex(corridorX, y1);
                        Vector2 v12 = vertex(corridorX, y2);
                        Vector2 v13 = vertex(corridorX, y3);
                        line(WALL, v10, v11);
                        line(DOOR, v11, v12);
                        line(WALL, v12, v13);
                        v00 = v03;
                        v10 = v13;
                    }
                }
                roomYs[i] = prevY + currentLength;
                Vector2 v0 = vertex(-wallX, v00.y);
                Vector2 v1 = vertex(wallX, v10.y);
                line(WALL, v00, v0);
                line(WALL, v10, v1);
            }
        }

    }
}
