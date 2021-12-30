package com.my.world.core;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScenesManager implements Disposable {

    private final Engine engine;

    @Getter
    private final Map<String, Scene> scenes = new LinkedHashMap<>();

    @Getter
    private Scene activatedScene;

    ScenesManager(Engine engine) {
        this.engine = engine;
    }

    public Scene newScene(String name) {
        if (scenes.containsKey(name)) throw new RuntimeException("Duplicate Scene: " + name);
        Scene scene = new Scene(engine, name);
        addScene(scene);
        return scene;
    }
    protected void addScene(Scene scene) {
        String name = scene.getName();
        if (scenes.containsKey(name)) throw new RuntimeException("Duplicate Scene: " + name);
        scenes.put(name, scene);
    }
    public void removeScene(String name) {
        if (!scenes.containsKey(name)) throw new RuntimeException("No Such Scene: " + name);
        Scene scene = scenes.remove(name);
        if (this.activatedScene == scene) this.activatedScene = null;
        scene.dispose();
    }
    public Scene getScene(String name) {
        if (!scenes.containsKey(name)) throw new RuntimeException("No Such Scene: " + name);
        return scenes.get(name);
    }
    public void setActivatedScene(String name) {
        activatedScene = getScene(name);
    }

    // ----- Load Scene ----- //
    public Scene loadSceneFromFile(String path) {
        String yamlConfig = LoaderManager.readFile(path);
        return loadSceneFromYaml(yamlConfig);
    }
    public Scene loadSceneFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.loadAs(yamlConfig, Map.class);
        return loadScene(config);
    }
    public Scene loadScene(Map<String, Object> config) {
        Scene scene = engine.getLoaderManager().load(config, Scene.class, engine.newContext());
        addScene(scene);
        return scene;
    }

    // ----- Dump Scene ----- //
    public void dumpSceneToFile(Scene scene, String path) {
        String yamlConfig = dumpSceneToYaml(scene);
        LoaderManager.writeFile(yamlConfig, path);
    }
    public String dumpSceneToYaml(Scene scene) {
        Map<String, Object> config = dumpScene(scene);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }
    public Map<String, Object> dumpScene(Scene scene) {
        return engine.getLoaderManager().dump(scene, Map.class, scene.newContext());
    }

    @Override
    public void dispose() {
        Disposable.disposeAll(scenes);
    }
}
