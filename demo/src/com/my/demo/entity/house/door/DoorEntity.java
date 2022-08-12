package com.my.demo.entity.house.door;

import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;

public class DoorEntity extends EnhancedEntity {

    public final static short DOOR_FLAG = 1 << 7;

    public final FrameEntity frame;
    public final RectHoleEntity hole;

    public DoorEntity(EnhancedContext context) {
        setName("Door");

        float width = context.get("DoorWidth", Float.class, 1f);
        float height = context.get("DoorHeight", Float.class, 2f);
        float depth = context.get("DoorDepth", Float.class, 1f);
        float depthOffset = context.get("DoorDepthOffset", Float.class, 0.01f);
        float edgeWidth = context.get("DoorEdgeWidth", Float.class, 0.1f);
        float padding = context.get("DoorPadding", Float.class, 0.01f);

        {
            EnhancedContext c = context.subContext();
            c.set("FrameWidth", width);
            c.set("FrameHeight", height);
            c.set("FrameDepth", depth + depthOffset);
            c.set("FrameEdgeWidth", edgeWidth);
            c.set("FramePadding", padding);

            frame = new FrameEntity(c);
            frame.setParent(this);
            frame.position.setLocalTransform(m -> m.setToTranslation(0, height / 2, 0));
            addEntity(frame);

            c.dispose();
        }

        {
            EnhancedContext c = context.subContext();
            c.set("HoleWidth", width);
            c.set("HoleHeight", height);
            c.set("HoleDepth", depth + depthOffset);
            c.copy("HoleMaterial", "WallMaterial");
            c.copy("HoleAttributes", "WallAttributes");

            hole = new RectHoleEntity(c);
            hole.setParent(this);
            hole.position.setLocalTransform(m -> m.setToTranslation(0, height / 2, 0));
            addEntity(hole);

            c.dispose();
        }
    }
}
