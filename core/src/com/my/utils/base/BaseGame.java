package com.my.utils.base;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public abstract class BaseGame extends ApplicationAdapter {

    protected InputMultiplexer inputMultiplexer = new InputMultiplexer();
    protected AssetManager assetManager;
    @Override
    public void create() {
        // Set InputMultiplexer
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create assetManager
        assetManager = new AssetManager();
        addDisposable(assetManager);
    }

    @Override
    public void render() {
        if (!isDoneLoading()) return;
        myRender();
    }

    @Override
    public void dispose() {
        for(int i = disposables.size - 1; i >= 0; i--) {
            Disposable disposable = disposables.get(i);
            if(disposable != null)
                disposable.dispose();
        }
    }

    // -------------------- Public & Protected -------------------- //

    // Handle Asset Loading
    private boolean loading = false;
    private boolean waitLoading = true;
    protected void waitLoad(boolean wait) {
        // Call this method manually before loading complete
        loading = true;
        this.waitLoading = wait;
    }
    protected boolean isDoneLoading() {
        if (loading) {
            if (assetManager.update()) { // Check assetManager
                loading = false;
                doneLoading();
            } else if (waitLoading) { // Check waitLoading
                return false;
            }
        }
        return true;
    }

    // Register Disposable Assets
    private Array<Disposable> disposables = new Array<>();
    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    // -------------------- Customizable -------------------- //

    // Custom doneLoading Code
    protected abstract void doneLoading();

    // Custom render Code
    protected abstract void myRender();
}
