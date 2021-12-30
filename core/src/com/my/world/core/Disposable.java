package com.my.world.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Disposable {

	void dispose();

	static void disposeAll(List<?> objs) {
		for (int i = objs.size() - 1; i >= 0; i--) {
			dispose(objs.get(i));
		}
		objs.clear();
	}

	static void disposeAll(Collection<?> objs) {
		for (Object obj : objs) {
			dispose(obj);
		}
		objs.clear();
	}

	static void disposeAll(Map<?, ?> objs) {
		for (Map.Entry<?, ?> entry : objs.entrySet()) {
			dispose(entry.getKey());
			dispose(entry.getValue());
		}
		objs.clear();
	}

	static void dispose(Object obj) {
		if (obj instanceof Disposable) {
			((Disposable) obj).dispose();
		}
	}
}
