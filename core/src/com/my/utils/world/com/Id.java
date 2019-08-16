package com.my.utils.world.com;

import com.my.utils.world.Component;

public class Id implements Component {

    public final String id;

    public Id(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
