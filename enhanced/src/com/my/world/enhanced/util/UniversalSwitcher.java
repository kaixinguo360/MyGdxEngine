package com.my.world.enhanced.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class UniversalSwitcher<E> extends Switcher<E> {

    protected final Function<String, E> itemGetter;
    protected final Consumer<E> itemEnabler;
    protected final Consumer<E> itemDisabler;

    public UniversalSwitcher(Function<String, E> itemGetter, Consumer<E> itemEnabler, Consumer<E> itemDisabler) {
        this.itemGetter = itemGetter;
        this.itemEnabler = itemEnabler;
        this.itemDisabler = itemDisabler;
    }

    protected E getItem(String name) {
        if (itemGetter == null) throw new RuntimeException("ItemGetter is null");
        return itemGetter.apply(name);
    }

    protected void enableItem(E item) {
        if (itemEnabler == null) throw new RuntimeException("ItemEnabler is null");
        itemEnabler.accept(item);
    }

    protected void disableItem(E item) {
        if (itemDisabler == null) throw new RuntimeException("ItemDisabler is null");
        itemDisabler.accept(item);
    }
}
