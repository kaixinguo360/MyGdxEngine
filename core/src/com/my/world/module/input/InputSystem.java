package com.my.world.module.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.Script;
import lombok.Getter;

import java.util.Collection;

public class InputSystem implements System, System.AfterAdded, System.OnStart, InputProcessor {

    @Getter
    private final InputMultiplexer inputMultiplexer;

    private Scene scene;
    private final EntityFilter keyDownFilter = entity -> entity.contain(OnKeyDown.class);
    private final EntityFilter keyUpFilter = entity -> entity.contain(OnKeyUp.class);
    private final EntityFilter keyTypedFilter = entity -> entity.contain(OnKeyTyped.class);
    private final EntityFilter touchDownFilter = entity -> entity.contain(OnTouchDown.class);
    private final EntityFilter touchUpFilter = entity -> entity.contain(OnTouchUp.class);
    private final EntityFilter touchDraggedFilter = entity -> entity.contain(OnTouchDragged.class);
    private final EntityFilter mouseMovedFilter = entity -> entity.contain(OnMouseMoved.class);
    private final EntityFilter scrolledFilter = entity -> entity.contain(OnScrolled.class);

    public InputSystem() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
    }

    @Override
    public void start(Scene scene) {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void afterAdded(Scene scene) {
        this.scene = scene;
        scene.getEntityManager().addFilter(keyDownFilter);
        scene.getEntityManager().addFilter(keyUpFilter);
        scene.getEntityManager().addFilter(keyTypedFilter);
        scene.getEntityManager().addFilter(touchDownFilter);
        scene.getEntityManager().addFilter(touchUpFilter);
        scene.getEntityManager().addFilter(touchDraggedFilter);
        scene.getEntityManager().addFilter(mouseMovedFilter);
        scene.getEntityManager().addFilter(scrolledFilter);
    }

    @Override
    public boolean keyDown(int keycode) {
        for (Entity entity : getEntities(keyDownFilter)) {
            for (OnKeyDown script : entity.getComponents(OnKeyDown.class)) {
                if (Component.isActive(script)) {
                    script.keyDown(keycode);
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (Entity entity : getEntities(keyUpFilter)) {
            for (OnKeyUp script : entity.getComponents(OnKeyUp.class)) {
                if (Component.isActive(script)) {
                    script.keyUp(keycode);
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (Entity entity : getEntities(keyTypedFilter)) {
            for (OnKeyTyped script : entity.getComponents(OnKeyTyped.class)) {
                if (Component.isActive(script)) {
                    script.keyTyped(character);
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (Entity entity : getEntities(touchDownFilter)) {
            for (OnTouchDown script : entity.getComponents(OnTouchDown.class)) {
                if (Component.isActive(script)) {
                    script.touchDown(screenX, screenY, pointer, button);
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (Entity entity : getEntities(touchUpFilter)) {
            for (OnTouchUp script : entity.getComponents(OnTouchUp.class)) {
                if (Component.isActive(script)) {
                    script.touchUp(screenX, screenY, pointer, button);
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (Entity entity : getEntities(touchDraggedFilter)) {
            for (OnTouchDragged script : entity.getComponents(OnTouchDragged.class)) {
                if (Component.isActive(script)) {
                    script.touchDragged(screenX, screenY, pointer);
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (Entity entity : getEntities(mouseMovedFilter)) {
            for (OnMouseMoved script : entity.getComponents(OnMouseMoved.class)) {
                if (Component.isActive(script)) {
                    script.mouseMoved(screenX, screenY);
                }
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        for (Entity entity : getEntities(scrolledFilter)) {
            for (OnScrolled script : entity.getComponents(OnScrolled.class)) {
                if (Component.isActive(script)) {
                    script.scrolled(amount);
                }
            }
        }
        return false;
    }

    private Collection<Entity> getEntities(EntityFilter entityFilter) {
        return scene.getEntityManager().getEntitiesByFilter(entityFilter);
    }

    public interface OnKeyDown extends Script {
        void keyDown(int keycode);
    }
    public interface OnKeyUp extends Script {
        void keyUp(int keycode);
    }
    public interface OnKeyTyped extends Script {
        void keyTyped(char character);
    }
    public interface OnTouchDown extends Script {
        void touchDown(int screenX, int screenY, int pointer, int button);
    }
    public interface OnTouchUp extends Script {
        void touchUp(int screenX, int screenY, int pointer, int button);
    }
    public interface OnTouchDragged extends Script {
        void touchDragged(int screenX, int screenY, int pointer);
    }
    public interface OnMouseMoved extends Script {
        void mouseMoved(int screenX, int screenY);
    }
    public interface OnScrolled extends Script {
        void scrolled(int amount);
    }
}
