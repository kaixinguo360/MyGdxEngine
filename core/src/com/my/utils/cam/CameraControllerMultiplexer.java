package com.my.utils.cam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;

/** An {@link InputProcessor} that delegates to an ordered list of CameraControllers. */
public class CameraControllerMultiplexer extends InputMultiplexer {

	// -------------------- Add & Get Controller -------------------- //

	private Array<Camera> processors = new Array<>(4);
	public void addProcessor (String name, CameraController processor, boolean cursorCatch) {
		processors.add(new Camera(name, processor, cursorCatch));
		change();
	}
	public CameraController getProcessor (String name) {
		for (Camera camera : processors) {
			if (camera.name.equals(name)) return camera.controller;
		}
		return null;
	}

	// -------------------- Change Controller -------------------- //

	private int activatedIndex = 0;
	private Camera activatedCamera = null;
	private int CHANGE = Input.Keys.TAB;

	// Change To Next Controller
	public void change() {
		change((activatedIndex + 1 + processors.size) % processors.size);
	}

	// Change Controller By The Given Name
	public void change(String name) {
		// Find Next Camera
		int index = 0;
		while (index < processors.size) {
			Camera camera = processors.get(index);
			if (name.equals(camera.name)) {
				change(index);
				return;
			}
			index++;
		}
		throw new RuntimeException("No Such Camera!");
	}

	// Change Controller By The Given Index
	private void change(int index) {
		// Remove Previous Camera
		if (super.size() != 0) super.removeProcessor(0);
		// Get Activated Camera By The Given Index
		activatedIndex = index;
		activatedCamera = processors.get(index);
		// Change CursorCatch Configure
		Gdx.input.setCursorCatched(activatedCamera.hideCursor);
		// Init Activated Camera
		activatedCamera.controller.init();
		super.addProcessor(activatedCamera.controller);
	}

	// Change Controller When CHANGE keyDown
	public boolean keyDown (int keycode) {
		if (keycode == CHANGE) {
			change();
		}
		return super.keyDown(keycode);
	}

	// -------------------- Other -------------------- //

	public String getActivatedCameraName() {
		return activatedCamera.name;
	}

	public void update() {
		if (activatedCamera.hideCursor) Gdx.input.setCursorPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		activatedCamera.controller.update();
	}

	// -------------------- Inner Class & Interface -------------------- //

	public interface CameraController extends InputProcessor {
		void init();
		void update();
	}

	private class Camera {
		private final String name;
		private final CameraController controller;
		private final boolean hideCursor;

		Camera(String name, CameraController controller, boolean hideCursor) {
			this.name = name;
			this.controller = controller;
			this.hideCursor = hideCursor;
		}
	}
}
