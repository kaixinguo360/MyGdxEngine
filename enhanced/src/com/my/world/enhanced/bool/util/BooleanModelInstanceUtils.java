package com.my.world.enhanced.bool.util;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.enhanced.bool.operation.BooleanOperationException;
import com.my.world.enhanced.bool.operation.ModelInstanceBoolOperation;

public class BooleanModelInstanceUtils {

    public static final short UNKNOWN = 0;
    public static final short UNION = 1;
    public static final short DIFF = 2;
    public static final short INTER = 4;

    public static void doUnion(ModelInstance target, MeshPart reference, Matrix4 transform) throws BooleanOperationException {
        booleanOperation(target, reference, transform, UNION);
    }

    public static void doDifference(ModelInstance target, MeshPart reference, Matrix4 transform) throws BooleanOperationException {
        booleanOperation(target, reference, transform, DIFF);
    }

    public static void doIntersection(ModelInstance target, MeshPart reference, Matrix4 transform) throws BooleanOperationException {
        booleanOperation(target, reference, transform, INTER);
    }

    private static void booleanOperation(ModelInstance target, MeshPart reference, Matrix4 transform, int type) throws BooleanOperationException {
        if (target == null || reference == null) return;

        ModelInstanceBoolOperation boolOperation = new ModelInstanceBoolOperation(target, reference, transform);
        switch (type) {
            case UNION: {
                boolOperation.doUnion();
                break;
            }
            case DIFF: {
                boolOperation.doDifference();
                break;
            }
            case INTER: {
                boolOperation.doIntersection();
                break;
            }
        }
        boolOperation.apply();
    }
}
