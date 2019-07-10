package com.my.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.my.game.base.BaseUI;

public class MyUI extends BaseUI {
    @Override
    protected void addWidgets() {
        // Add List
        List<String> list = new List<>(skin);
        list.setItems("123", "234", "345", "test");
        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Selected: " + ((List) actor).getSelected());
            }
        });
        addWidget("list", list);

        // Add Button1, Button2
        addWidget("button1", new TextButton("Click me!", skin));
        addWidget("button2", new TextButton("Click me2!", skin));

        // Add Button3
        TextButton button = new TextButton("Click me3!", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Button3 Clicked");
            }
        });
        addWidget("button3", button);
    }
}
