package com.my.demo.entity.house;

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

        for (int i = 1; i <= floorsNum; i++) {

            c.setPrefix("楼层" + i);

            RoomEntity singleFloor = new RoomEntity(c);
            singleFloor.transform.setToTranslation(0, singleFloorY, 0);
            singleFloor.decompose();
            singleFloor.setParent(this);
            addEntity(singleFloor);
            floors.add(singleFloor);

            singleFloorY += singleFloor.render.roomHeight;
        }

        c.dispose();
    }
}
