package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public abstract class BaseSystem implements System {

    protected final Array<Entity> entities = new Array<>();

    @Override
    public Array<Entity> getEntities() {
        return entities;
    }

    // ----- Dispose ----- //
    @Override
    public void dispose() {
        for(int i = disposables.size - 1; i >= 0; i--) {
            Disposable disposable = disposables.get(i);
            if(disposable != null)
                disposable.dispose();
        }
    }
    private Array<Disposable> disposables = new Array<>();
    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }
}
