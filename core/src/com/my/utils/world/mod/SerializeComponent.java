package com.my.utils.world.mod;

import com.my.utils.world.Component;

public class SerializeComponent implements Component {

    // ----- Properties ----- //
    private Serializer serializer;
    String name;

    // ----- Constructor ----- //
    public SerializeComponent(Serializer serializer, String name) {
        this.serializer = serializer;
        this.name = name;
    }

    // ----- Custom ----- //
    String serialize() {
        return serializer.serialize();
    }
    void deserialize(String status) {
        serializer.deserialize(status);
    }

    public interface Serializer {
        String serialize();
        void deserialize(String data);
    }
}
