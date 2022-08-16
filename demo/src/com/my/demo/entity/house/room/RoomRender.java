package com.my.demo.entity.house.room;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.house.room.blueprint.Line;
import com.my.demo.entity.house.room.blueprint.PlaneBlueprint;
import com.my.demo.entity.house.room.blueprint.Point;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.module.render.model.ProceduralModelRender;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import static com.my.demo.entity.house.room.CorridorRoomBlueprint.*;

public class RoomRender extends ProceduralModelRender {

    public static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    public static final Material MATERIAL = new Material(PBRColorAttribute.createBaseColorFactor(Color.WHITE));

    public final float roomWidth;
    public final float roomLength;
    public final float roomHeight;
    public final float roomWallThickness;
    public final float roomFloorThickness;

    public final float wallThickness;
    public final Material wallMaterial;

    public final float floorThickness;
    public final Material floorMaterial;

    public final float pillarThick;
    public final Material pillarMaterial;

    public final float doorHeight;

    public final float wallHeight;
    public final float wallCenterY;

    public RoomRender(EnhancedContext context, PlaneBlueprint blueprint) {
        mdBuilder.begin();

        roomWidth = context.get("房间宽度", Float.class, 10f);
        roomLength = context.get("房间长度", Float.class, 10f);
        roomHeight = context.get("房间高度", Float.class, 3.5f);
        roomWallThickness = context.get("房间墙厚度", Float.class, 0.2f);
        roomFloorThickness = context.get("房间地板厚度", Float.class, 0.4f);

        wallThickness = context.get("墙厚度", Float.class, roomWallThickness);
        wallMaterial = context.get("墙材质", Material.class, MATERIAL);

        floorThickness = context.get("地板厚度", Float.class, roomFloorThickness);
        floorMaterial = context.get("地板材质", Material.class, MATERIAL);

        pillarThick = context.get("柱子粗细", Float.class, roomWallThickness);
        pillarMaterial = context.get("柱子材质", Material.class, MATERIAL);

        doorHeight = context.get("门高度", Float.class, 2f);

        wallHeight = roomHeight - floorThickness;
        wallCenterY = floorThickness + wallHeight / 2;

        // Build Floor
        float floorCenterY = floorThickness / 2;
        float floorWidth = roomWidth + pillarThick;
        float floorLength = roomLength + pillarThick;
        MeshPartBuilder floorsPart = mdBuilder.part("floors", GL20.GL_TRIANGLES, ATTRIBUTES, floorMaterial);
        buildBox(
                floorsPart,
                floorWidth, floorThickness, floorLength,
                0, floorCenterY, 0,
                0
        );

        // Build Walls & Doors
        MeshPartBuilder wallsPart = null;
        for (Line line : blueprint.lines) {
            if (wallsPart == null) {
                wallsPart = mdBuilder.part("walls", GL20.GL_TRIANGLES, ATTRIBUTES, wallMaterial);
            }
            switch (line.tag) {
                case WALL:
                    buildWall(wallsPart, line);
                    break;
                case DOOR:
                    buildDoor(wallsPart, line);
                    break;
                default:
                    throw new RuntimeException("No such tag handler: " + line.tag);
            }
        }

        // Build Pillars
        MeshPartBuilder pillarsPart = null;
        for (Point point : blueprint.points) {
            if (pillarsPart == null) {
                pillarsPart = mdBuilder.part("pillars", GL20.GL_TRIANGLES, ATTRIBUTES, pillarMaterial);
            }
            switch (point.tag) {
                case PILLAR:
                    buildPillar(pillarsPart, point);
                    break;
                default:
                    throw new RuntimeException("No such tag handler: " + point.tag);
            }
        }

        model = mdBuilder.end();
        init();
    }

    private void buildPillar(MeshPartBuilder part, Point point) {
        buildBox(
                part,
                pillarThick, wallHeight, pillarThick,
                point.center.x,
                wallCenterY,
                point.center.y,
                point.angleDeg()
        );
    }

    private void buildWall(MeshPartBuilder part, Line line) {
        buildBox(
                part,
                line.len(), wallHeight, wallThickness,
                (line.a.x + line.b.x) / 2,
                wallCenterY,
                (line.a.y + line.b.y) / 2,
                line.angleDeg()
        );
    }

    private void buildDoor(MeshPartBuilder part, Line line) {
        buildBox(
                part,
                line.len(), wallHeight - doorHeight, wallThickness,
                (line.a.x + line.b.x) / 2,
                wallCenterY + doorHeight / 2,
                (line.a.y + line.b.y) / 2,
                line.angleDeg()
        );
    }

    private void buildBox(MeshPartBuilder part, float width, float height, float depth, float centerX, float centerY, float centerZ, float angle) {
        part.setVertexTransform(new Matrix4().setToTranslation(centerX, centerY, centerZ).rotate(Vector3.Y, angle));
        BoxShapeBuilder.build(part, width, height, depth);
    }
}
