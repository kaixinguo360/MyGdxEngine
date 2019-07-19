package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseModule<T extends Component> implements Module, Disposable {

    // ----- Component ----- //
    protected final Map<T, String> components = new HashMap<>();
    public void add(Component component, String instanceName) {
        if (!handle(component)) throw new IllegalArgumentException();
        T componentT = (T) component;
        components.put(componentT, instanceName);
        addComponent(componentT);
    }
    public String get(Component component) {
        return components.get(component);
    }
    public void remove(Component component) {
        if (!handle(component)) throw new IllegalArgumentException();
        T componentT = (T) component;
        removeComponent(componentT);
        components.remove(componentT);
    }

    // ----- Protected ----- //
    protected void addComponent(T component) {}
    protected void removeComponent(T component) {}

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
