package com.my.world.core;

import lombok.Getter;

public class Engine implements Disposable {

    public static final String CONTEXT_FIELD_NAME = "ENGINE";

    @Getter
    protected final AssetsManager assetsManager;

    @Getter
    protected final LoaderManager loaderManager;

    @Getter
    protected final ScenesManager scenesManager;

    @Getter
    protected final Context context;

    public Engine() {
        assetsManager = new AssetsManager(this);
        loaderManager = new LoaderManager(this);
        scenesManager = new ScenesManager(this);
        context = new Context(null);
        context.setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, assetsManager);
        context.setEnvironment(LoaderManager.CONTEXT_FIELD_NAME, loaderManager);
        context.setEnvironment(Engine.CONTEXT_FIELD_NAME, this);
    }

    public void update(float deltaTime) {
        Scene activatedScene = scenesManager.getActivatedScene();
        if (activatedScene == null) throw new RuntimeException("No Activated Scene");
        activatedScene.update(deltaTime);
    }

    public Context newContext() {
        return this.context.newContext();
    }

    @Override
    public void dispose() {
        scenesManager.dispose();
    }
}
