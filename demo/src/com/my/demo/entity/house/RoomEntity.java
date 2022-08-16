package com.my.demo.entity.house;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.demo.entity.house.room.CorridorRoomBlueprint;
import com.my.demo.entity.house.room.RoomRender;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.TemplateRigidBody;

public class RoomEntity extends EnhancedEntity {

    public final CorridorRoomBlueprint blueprint;
    public final RoomRender render;
    public final TemplateRigidBody rigidBody;

    public RoomEntity(EnhancedContext context) {
        setName("Room");

        blueprint = new CorridorRoomBlueprint() {
            @Override
            protected void config() {
                randomSeed = context.get("随机数种子", Long.class, 10000L);
                roomWidth = context.get("房间宽度", Float.class, 10f);
                roomLength = context.get("房间长度", Float.class, 10f);
                doorWidth = context.get("门宽度", Float.class, 1f);
                corridorWidth = context.get("走廊宽度", Float.class, doorWidth * 2);
                roomMinLength = context.get("房间最小长度", Float.class, doorWidth * 2);
            }
        };

        render = addComponent(new RoomRender(context, blueprint));
        rigidBody = addComponent(new TemplateRigidBody(Bullet.obtainStaticNodeShape(render.modelInstance.nodes), 0));
    }
}
