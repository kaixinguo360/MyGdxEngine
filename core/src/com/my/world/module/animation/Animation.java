package com.my.world.module.animation;

import com.my.world.core.*;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.SyncComponent;
import com.my.world.module.script.ScriptSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Animation extends ActivatableComponent implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, Configurable.OnLoad, Configurable.OnDump {

    @Config(type = Config.Type.Asset)
    public AnimationController animationController;

    @Config
    public boolean useGlobalTime;

    @Config
    @Setter
    @Getter
    private float currentTime;

    public Animation() {}

    public Animation(Playable playable) {
        addPlayable("default", playable);
    }

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
        this.entity = entity;
        if (useGlobalTime) {
            currentTime = scene.getTimeManager().getCurrentTime();
        } else {
            currentTime = 0;
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {

        // Update currentTime
        currentTime += scene.getTimeManager().getDeltaTime();

        // Update playable
        if (animationController != null) {
            animationController.update(currentTime, 1, this);
        } else {
            playables.values().stream().findFirst().ifPresent(playable -> playable.update(currentTime, 1, this));
        }

        // Sync changedSyncComponents
        for (SyncComponent component : changedSyncComponents) {
            component.sync();
        }
        changedSyncComponents.clear();
        changedChannels.clear();
    }

    // ----- Playables ----- //

    public final Map<String, Playable> playables = new LinkedHashMap<>();

    public <T extends Playable> T addPlayable(String name, T playable) {
        if (playables.containsKey(name)) throw new RuntimeException("Duplicate playable: " + name);
        playables.put(name, playable);
        return playable;
    }

    public void removePlayable(String name) {
        if (!playables.containsKey(name)) throw new RuntimeException("No such playable: " + name);
        playables.remove(name);
    }

    public Playable getPlayable(String name) {
        if (!playables.containsKey(name)) throw new RuntimeException("No such playable: " + name);
        return playables.get(name);
    }

    public <T extends Playable> T getPlayable(String name, Class<T> type) {
        return (T) getPlayable(name);
    }

    // ----- Load & Dump ----- //

    @Override
    public void load(Map<String, Object> config, Context context) {
        Configurable.load(this, config, context);
        Map<String, String> playableConfigs = (Map<String, String>) config.get("playables");
        AssetsManager assetsManager = context.get(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
        playables.clear();
        playableConfigs.forEach((name, value) -> {
            Playable playable = assetsManager.getAsset(value, Playable.class);
            playables.put(name, playable);
        });
    }

    @Override
    public Map<String, Object> dump(Context context) {
        Map<String, Object> config = Configurable.dump(this, context);
        Map<String, Object> playableConfigs = new HashMap<>();
        config.put("playables", playableConfigs);
        AssetsManager assetsManager = context.get(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
        playables.forEach((name, value) -> {
            String id = assetsManager.getId(Playable.class, value);
            playableConfigs.put(name, id);
        });
        return config;
    }

    // ----- Animation Context ----- //

    public Scene scene;
    public Entity entity;
    public AnimationController.Instance controllerInstance;
    public final Set<SyncComponent> changedSyncComponents = new HashSet<>();
    public final Set<String> changedChannels = new HashSet<>();
    public final Map<String, AnimationChannel.Instance> channelInstanceCaches = new HashMap<>();

}
