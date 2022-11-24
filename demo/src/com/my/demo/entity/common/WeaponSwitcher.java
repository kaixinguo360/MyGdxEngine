package com.my.demo.entity.common;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.util.EntitySwitcher;
import com.my.world.enhanced.util.SimpleGuiDrawer;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.input.InputSystem;

public class WeaponSwitcher extends EntitySwitcher implements CameraSystem.AfterAllRender, InputSystem.OnScrolled, InputSystem.OnTouchDown, InputSystem.OnKeyDown {

    @Config public final Color selectedColor = new Color(0, 0, 0, 0.2f);
    @Config public final Color normalColor = new Color(1, 1, 1, 0.2f);
    @Config public Float size;
    @Config public float sizePercentage = 0.2f;
    @Config public Float margin;
    @Config public float marginPercentage = 0.1f;
    @Config public Float y;
    @Config public boolean enableScrollSwitching = true;
    @Config public int fastSwitchingKey = Input.Keys.Q;
    @Config public int fastSwitchingButton = Input.Buttons.RIGHT;
    @Config public int nextKey = -1;
    @Config public int nextButton = -1;
    @Config public int prevKey = -1;
    @Config public int prevButton = -1;

    protected SimpleGuiDrawer drawer;

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);
        drawer = new SimpleGuiDrawer();
        drawer.origin.set(0.5f, 0);
    }

    @Override
    public void afterAllRender() {
        drawer.begin();
        float size = this.size != null ? this.size : drawer.height(sizePercentage);
        float margin = this.margin != null ? this.margin : size * marginPercentage;
        float y = this.y != null ? this.y : size / 2;
        float xStart = -((size * items.size()) / 2 - size / 2);
        for (int i = 0; i < items.size(); i++) {
            drawer.setColor((i == activeIndex) ? selectedColor : normalColor);
            drawer.square(xStart + size * i, y, size - margin * 2);
        }
        drawer.end();
    }

    @Override
    public void scrolled(float amountX, float amountY) {
        if (!enableScrollSwitching) return;
        if (amountY > 0) {
            next();
        } else if (amountY < 0) {
            prev();
        }
    }

    @Override
    public void touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == fastSwitchingButton) last();
        if (button == nextButton) next();
        if (button == prevButton) prev();

    }

    @Override
    public void keyDown(int keycode) {
        if (Input.Keys.NUM_1 <= keycode && keycode <= Input.Keys.NUM_9) {
            int nextIndex = keycode - Input.Keys.NUM_1;
            if (nextIndex < items.size()) {
                switchTo(nextIndex);
            }
        }
        if (keycode == fastSwitchingKey) last();
        if (keycode == nextKey) next();
        if (keycode == prevKey) prev();
    }
}
