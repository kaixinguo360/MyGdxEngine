package com.my.demo.entity.house;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;

import java.util.ArrayList;
import java.util.List;

public class HouseEntity extends EnhancedEntity {

    public final List<RoomEntity> floors = new ArrayList<>();

    public HouseEntity(EnhancedContext context) {
        setName("House");

        int floorsNum = context.get("楼层数", Integer.class, 2);
        float floorHeight = context.get("楼层高", Float.class, 3.5f);
        float width = context.get("楼宽度", Float.class, 10f);
        float length = context.get("楼长度", Float.class, 10f);

        float singleFloorY = 0;
        EnhancedContext c = context.subContext();
        c.set("房间高度", floorHeight);
        c.set("房间宽度", width);
        c.set("房间长度", length);
        c.set("楼梯高度", floorHeight);

        for (int i = 1; i <= floorsNum; i++) {

            c.setPrefix("楼层" + i);

            RoomEntity singleFloor = new RoomEntity(c);
            singleFloor.transform.setToTranslation(0, singleFloorY, 0);
            singleFloor.decompose();
            singleFloor.setParent(this);
            addEntity(singleFloor);
            floors.add(singleFloor);

            StairsEntity stairs = new StairsEntity(c);
            stairs.transform.setToTranslation(
                    -stairs.stairsLength / 2 + stairs.stairsPlatformSize / 2,
                    singleFloorY,
                    -length / 2 - stairs.stairsWidth / 2
            ).rotate(Vector3.Y, -90);
            stairs.decompose();
            stairs.setParent(this);
            addEntity(stairs);

            singleFloorY += singleFloor.render.roomHeight;
        }

        c.dispose();
    }
}
