package com.my.demo.entity.house;

import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.constraint.FixedConstraint;

import java.util.ArrayList;
import java.util.List;

public class HouseEntity extends EnhancedEntity {

    public final List<RoomEntity> floors = new ArrayList<>();

    public HouseEntity(EnhancedContext context) {
        setName("House");

        int floorsNum = context.get("HouseFloorsNum", Integer.class, 2);
        float floorHeight = context.get("HouseFloorHeight", Float.class, 3.5f);
        float width = context.get("HouseWidth", Float.class, 10f);
        float length = context.get("HouseLength", Float.class, 10f);
        float breakingImpulseThreshold = context.get("BreakingImpulseThreshold", Float.class, 0f);

        float singleFloorY = 0;
        EnhancedContext c = context.subContext();
        c.set("RoomHeight", floorHeight);
        c.set("RoomWidth", width);
        c.set("RoomLength", length);

        for (int i = 1; i <= floorsNum; i++) {

            c.setPrefix("floor-" + i);

            RoomEntity singleFloor = i == 1 ? new EnhancedRoomEntity(c) : new RoomEntity(c);
            singleFloor.transform.setToTranslation(0, singleFloorY, 0);
            singleFloor.decompose();
            singleFloor.setParent(this);
            addEntity(singleFloor);
            floors.add(singleFloor);

            singleFloorY += c.get("RoomHeight", Float.class, 3.5f);

            if (i > 1) {
                RoomEntity prevFloor = floors.get(i - 2);

//                String curName = "floor-" + i;
//                String prevName = "floor-" + (i - 1);
//
//                PortalEntity portal1 = new PortalEntity(1);
//                portal1.setName(curName);
//                portal1.setParent(singleFloor);
//                portal1.position.getLocalTransform().setToTranslation(5, 1.2f + singleFloor.floorThickness, 0);
//                portal1.portal.targetPortalName = prevName;
//                portal1.rigidBody.mask = CharacterFilter;
//                addEntity(portal1);
//
//                PortalEntity portal2 = new PortalEntity(1);
//                portal2.setName(prevName);
//                portal2.setParent(prevFloor);
//                portal2.position.getLocalTransform().setToTranslation(-5, 1.2f + prevFloor.floorThickness, 0);
//                portal2.portal.targetPortalName = curName;
//                portal2.rigidBody.mask = CharacterFilter;
//                addEntity(portal2);

                if (breakingImpulseThreshold != 0) {
                    for (PillarEntity pillar : prevFloor.pillars) {
                        FixedConstraint.connect(singleFloor.floor, pillar, pillar.mass * breakingImpulseThreshold);
                    }
                    for (WallEntity wall : prevFloor.walls) {
                        FixedConstraint.connect(singleFloor.floor, wall, wall.mass * breakingImpulseThreshold);
                    }
                }
            }
        }

        c.dispose();
    }
}
