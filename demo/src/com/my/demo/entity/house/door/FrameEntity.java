package com.my.demo.entity.house.door;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.EnhancedContext;
import com.my.world.enhanced.entity.EnhancedEntity;

import static com.my.demo.entity.house.door.DoorEntity.DOOR_FLAG;

public class FrameEntity extends EnhancedEntity {

    public FrameEntity(EnhancedContext context) {
        setName("Frame");

        float width = context.get("FrameWidth", Float.class, 1f);
        float height = context.get("FrameHeight", Float.class, 2f);
        float depth = context.get("FrameDepth", Float.class, 1f);
        float edgeWidth = context.get("FrameEdgeWidth", Float.class, 0.1f);
        float padding = context.get("FramePadding", Float.class, 0.01f);
        boolean isKinematic = context.get("FrameIsKinematic", Boolean.class, false);

        float edgeX = width / 2 + edgeWidth / 2 - padding;
        float edgeY = height / 2 + edgeWidth / 2 - padding;

        {
            EnhancedContext commonContext = context.subContext();
            commonContext.set("EdgeDepth", depth);
            commonContext.set("EdgeWidth", edgeWidth);

            {
                EnhancedContext c = commonContext.subContext();
                c.set("EdgeLength", width + (edgeWidth - padding) * 2);

                EdgeEntity edgeBottom = new EdgeEntity(c);
                edgeBottom.setParent(this);
                edgeBottom.rigidBody.group = DOOR_FLAG;
                edgeBottom.rigidBody.isKinematic = isKinematic;
                edgeBottom.position.setLocalTransform(m -> m.setToTranslation(0, -edgeY, 0));
                addEntity(edgeBottom);

                EdgeEntity edgeUp = new EdgeEntity(c);
                edgeUp.setParent(this);
                edgeUp.rigidBody.group = DOOR_FLAG;
                edgeUp.rigidBody.isKinematic = isKinematic;
                edgeUp.position.setLocalTransform(m -> m.setToTranslation(0, edgeY, 0).rotate(Vector3.Z, 180));
                addEntity(edgeUp);

                c.dispose();
            }

            {
                EnhancedContext c = commonContext.subContext();
                c.set("EdgeLength", height);

                EdgeEntity edgeLeft = new EdgeEntity(c);
                edgeLeft.setParent(this);
                edgeLeft.rigidBody.group = DOOR_FLAG;
                edgeLeft.rigidBody.isKinematic = isKinematic;
                edgeLeft.position.setLocalTransform(m -> m.setToTranslation(-edgeX, 0, 0).rotate(Vector3.Z, -90));
                addEntity(edgeLeft);

                EdgeEntity edgeRight = new EdgeEntity(c);
                edgeRight.setParent(this);
                edgeRight.rigidBody.group = DOOR_FLAG;
                edgeRight.rigidBody.isKinematic = isKinematic;
                edgeRight.position.setLocalTransform(m -> m.setToTranslation(edgeX, 0, 0).rotate(Vector3.Z, 90));
                addEntity(edgeRight);

                c.dispose();
            }

            commonContext.dispose();
        }
    }
}
