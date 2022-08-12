package com.my.demo.entity.house;

import com.my.demo.entity.house.door.DoorEntity;
import com.my.world.enhanced.EnhancedContext;

public class EnhancedRoomEntity extends RoomEntity {

    public EnhancedRoomEntity(EnhancedContext context) {
        super(context);
        setName("EnhancedRoom");

        {
            EnhancedContext c = context.subContext();
            c.set("DoorDepth", wallThickness);
            c.copy("HoleMaterial", "WallMaterial");
            c.copy("HoleAttributes", "WallAttributes");

            DoorEntity door = buildDoor(c, this.wallForward, 0, 0);
            addEntity(door);

            c.dispose();
        }
    }

    private DoorEntity buildDoor(EnhancedContext c, WallEntity wallEntity, float x, float y) {
        DoorEntity door = new DoorEntity(c);
        door.setParent(wallEntity);
        door.position.setLocalTransform(m -> m.setToTranslation(x, -wallEntity.height / 2 + y, 0));
        door.hole.addRendersFromEntity(wallEntity, false);
        wallEntity.rigidBody.mask &= ~DoorEntity.DOOR_FLAG;
        door.hole.rigidBody.mask &= ~wallEntity.rigidBody.group;
        door.hole.holeScript.group = wallEntity.rigidBody.group;
        return door;
    }
}
