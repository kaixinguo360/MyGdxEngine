package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;

public interface Instance extends Disposable {
    Iterable<Module> getModules();
}
