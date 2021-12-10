package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Collection;

public abstract class BaseSystem implements System, AfterAdded {

    private World world;
    private EntityFilter entityFilter;

    @Override
    public void afterAdded(World world) {
        this.world = world;
        this.entityFilter = BaseSystem.this::isHandleable;
        world.getEntityManager().addFilter(entityFilter);
    }

    // ----- Entities ----- //
    protected Collection<? extends Entity> getEntities() {
        return world.getEntityManager().getEntitiesByFilter(entityFilter);
    }
    protected abstract boolean isHandleable(Entity entity);

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
