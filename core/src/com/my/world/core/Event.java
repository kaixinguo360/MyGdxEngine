package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.Getter;

public class Event implements Disposable {

    @Getter
    private Object source;

    @Getter
    private Object data;

    public Event(Object source, Object data) {
        this.source = source;
        this.data = data;
    }

    public <T> T getSource(Class<T> type) {
        return (T) source;
    }

    public <T> T getData(Class<T> type) {
        return (T) data;
    }

    @Override
    public void dispose() {
        source = null;
        data = null;
    }
}
