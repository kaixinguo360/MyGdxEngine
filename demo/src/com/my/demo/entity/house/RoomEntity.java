package com.my.demo.entity.house;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.constraint.FixedConstraint;

import java.util.ArrayList;
import java.util.List;

public class RoomEntity extends EnhancedEntity {

    public final FloorEntity floor;
    public final WallEntity wallLeft;
    public final WallEntity wallRight;
    public final WallEntity wallBack;
    public final WallEntity wallForward;
    public final List<WallEntity> walls = new ArrayList<>();
    public final List<PillarEntity> pillars = new ArrayList<>();

    protected final float breakingImpulseThreshold;
    protected final float width;
    protected final float length;
    protected final float height;
    protected final float wallThickness;
    protected final float floorThickness;
    protected final float floorCenterY;
    protected final float wallHeight;
    protected final float wallCenterY;

    public RoomEntity(EnhancedContext context) {
        setName("Room");

        breakingImpulseThreshold = context.get("BreakingImpulseThreshold", Float.class, 0f);

        width = context.get("RoomWidth", Float.class, 10f);
        length = context.get("RoomLength", Float.class, 10f);
        height = context.get("RoomHeight", Float.class, 3.5f);
        wallThickness = context.get("RoomWallThickness", Float.class, 0.2f);
        floorThickness = context.get("RoomFloorThickness", Float.class, 0.4f);

        floorCenterY = floorThickness / 2;
        wallHeight = height - floorThickness;
        wallCenterY = floorThickness + wallHeight / 2;

        EnhancedContext commonContext = context.subContext();
        commonContext.set("FloorThickness", floorThickness);
        commonContext.set("WallThickness", wallThickness);
        commonContext.set("WallHeight", wallHeight);
        commonContext.set("PillarThick", wallThickness);
        commonContext.set("PillarHeight", wallHeight);

        {
            EnhancedContext c = commonContext.subContext();
            c.set("FloorWidth", width);
            c.set("FloorLength", length);

            c.setPrefix("floor");
            floor = new FloorEntity(c);
            floor.position.setLocalTransform(m -> m.setToTranslation(0, floorCenterY, 0));
            floor.setParent(this);
            addEntity(floor);

            c.dispose();
        }

        {
            EnhancedContext c = commonContext.subContext();
            c.set("WallLength", length - wallThickness * 2);

            c.setPrefix("wallLeft");
            wallLeft = new WallEntity(c);
            wallLeft.position.setLocalTransform(m -> m.setToTranslation(-width / 2 + wallThickness / 2, wallCenterY, 0).rotate(Vector3.Y, 90));
            addWall(wallLeft);

            c.setPrefix("wallRight");
            wallRight = new WallEntity(c);
            wallRight.position.setLocalTransform(m -> m.setToTranslation(width / 2 - wallThickness / 2, wallCenterY, 0).rotate(Vector3.Y, -90));
            addWall(wallRight);

            c.dispose();
        }

        {
            EnhancedContext c = commonContext.subContext();
            c.set("WallLength", width - wallThickness * 2);

            c.setPrefix("wallBack");
            wallBack = new WallEntity(c);
            wallBack.position.setLocalTransform(m -> m.setToTranslation(0, wallCenterY, length / 2 - wallThickness / 2));
            addWall(wallBack);

            c.setPrefix("wallForward");
            wallForward = new WallEntity(c);
            wallForward.position.setLocalTransform(m -> m.setToTranslation(0, wallCenterY, -length / 2 + wallThickness / 2).rotate(Vector3.Y, 180));
            addWall(wallForward);

            c.dispose();
        }

        {
            EnhancedContext c = commonContext.subContext();

            c.setPrefix("pillar1");
            PillarEntity pillar1 = new PillarEntity(c);
            pillar1.position.setLocalTransform(m -> m.setToTranslation(width / 2 - wallThickness / 2, wallCenterY, length / 2 - wallThickness / 2));
            addPillar(pillar1);

            c.setPrefix("pillar2");
            PillarEntity pillar2 = new PillarEntity(c);
            pillar2.position.setLocalTransform(m -> m.setToTranslation(width / 2 - wallThickness / 2, wallCenterY, -length / 2 + wallThickness / 2));
            addPillar(pillar2);

            c.setPrefix("pillar3");
            PillarEntity pillar3 = new PillarEntity(c);
            pillar3.position.setLocalTransform(m -> m.setToTranslation(-width / 2 + wallThickness / 2, wallCenterY, length / 2 - wallThickness / 2));
            addPillar(pillar3);

            c.setPrefix("pillar4");
            PillarEntity pillar4 = new PillarEntity(c);
            pillar4.position.setLocalTransform(m -> m.setToTranslation(-width / 2 + wallThickness / 2, wallCenterY, -length / 2 + wallThickness / 2));
            addPillar(pillar4);

            c.dispose();
        }
    }

    protected void addWall(WallEntity entity) {
        entity.setParent(this);
        if (breakingImpulseThreshold != 0) {
            FixedConstraint.connect(floor, entity, entity.mass * breakingImpulseThreshold);
        }
        addEntity(entity);
        walls.add(entity);
    }

    protected void addPillar(PillarEntity entity) {
        entity.setParent(this);
        if (breakingImpulseThreshold != 0) {
            FixedConstraint.connect(floor, entity, entity.mass * breakingImpulseThreshold);
        }
        addEntity(entity);
        pillars.add(entity);
    }
}
