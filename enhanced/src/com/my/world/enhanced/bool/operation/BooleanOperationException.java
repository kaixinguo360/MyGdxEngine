package com.my.world.enhanced.bool.operation;

import com.badlogic.gdx.graphics.g3d.model.MeshPart;

public class BooleanOperationException extends Exception {

    public MeshPart meshPart1;
    public MeshPart meshPart2;

    BooleanOperationException(String info) {
        super(info);
    }

    BooleanOperationException(String info, MeshPart meshPart1, MeshPart meshPart2) {
        super(info);
        this.meshPart1 = meshPart1;
        this.meshPart2 = meshPart2;
    }
}
