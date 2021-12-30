package com.my.world.gdx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class DisposableManager implements com.my.world.core.Disposable {

    private final Array<Disposable> disposables = new Array<>();

    public void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void dispose() {
        for(int i = disposables.size - 1; i >= 0; i--) {
            Disposable disposable = disposables.get(i);
            if(disposable != null)
                disposable.dispose();
        }
        disposables.clear();
    }
}
