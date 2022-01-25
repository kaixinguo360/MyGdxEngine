package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SceneManager implements Disposable {

    private final Engine engine;

    @Getter
    private final Map<String, Scene> scenes = new LinkedHashMap<>();

    SceneManager(Engine engine) {
        this.engine = engine;
    }

    public Scene newScene(String name) {
        if (scenes.containsKey(name)) throw new RuntimeException("Duplicate Scene: " + name);
        Scene scene = new Scene(engine, name);
        addScene(scene);
        return scene;
    }
    protected Scene addScene(Scene scene) {
        if (scene.getId() == null) scene.setId(scene.getName() + UUID.randomUUID());
        String id = scene.getId();
        if (scenes.containsKey(id)) throw new RuntimeException("Duplicate Scene: id=" + id);
        if (scene.getStatus() != Scene.Status.Created)
            throw new RuntimeException("Illegal Status(" + scene.getStatus() + ") of the Scene: id=" + scene.getId());
        scenes.put(id, scene);
        return scene;
    }
    public void removeScene(String id) {
        if (!scenes.containsKey(id)) throw new RuntimeException("No Such Scene: id=" + id);
        Scene scene = scenes.get(id);
        switch (scene.getStatus()) {
            case Created:
                scenes.remove(id);
                break;
            case Running:
                scene.setStatus(Scene.Status.Deleted);
                break;
            case Deleted:
                break;
            case Disposed:
                throw new RuntimeException("Illegal Status(" + scene.getStatus() + ") of the Scene: id=" + scene.getId());
        }
    }
    public Scene findSceneById(String id) {
        if (!scenes.containsKey(id)) throw new RuntimeException("No Such Scene: id=" + id);
        return scenes.get(id);
    }
    public Scene findSceneByName(String name) {
        if (name == null) throw new RuntimeException("Name of scene can not be null");
        for (Scene scene : scenes.values()) {
            if (name.equals(scene.getName())) {
                return scene;
            }
        }
        throw new RuntimeException("No Such Scene: name=" + name);
    }

    public void update(float deltaTime) {
        Iterator<Scene> it = scenes.values().iterator();
        while (it.hasNext()) {
            Scene scene = it.next();
            switch (scene.getStatus()) {
                case Created:
                    scene.setStatus(Scene.Status.Running);
                    scene.start();
                case Running:
                    scene.update(deltaTime);
                    break;
                case Deleted:
                    it.remove();
                    scene.dispose();
                    scene.setStatus(Scene.Status.Disposed);
                    break;
            }
        }
    }

    // ----- Load Scene ----- //
    public Scene loadSceneFromFile(String path) {
        String yamlConfig = SerializerManager.readFile(path);
        return loadSceneFromYaml(yamlConfig);
    }
    public Scene loadSceneFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.loadAs(yamlConfig, Map.class);
        return loadScene(config);
    }
    public Scene loadScene(Map<String, Object> config) {
        Scene scene = engine.getSerializerManager().load(config, Scene.class, engine.newContext());
        addScene(scene);
        return scene;
    }

    // ----- Dump Scene ----- //
    public void dumpSceneToFile(Scene scene, String path) {
        String yamlConfig = dumpSceneToYaml(scene);
        SerializerManager.writeFile(yamlConfig, path);
    }
    public String dumpSceneToYaml(Scene scene) {
        Map<String, Object> config = dumpScene(scene);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }
    public Map<String, Object> dumpScene(Scene scene) {
        return engine.getSerializerManager().dump(scene, Map.class, scene.newContext());
    }

    @Override
    public void dispose() {
        Disposable.disposeAll(scenes);
    }
}
