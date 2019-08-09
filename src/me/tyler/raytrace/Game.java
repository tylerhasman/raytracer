package me.tyler.raytrace;

import java.awt.*;

public abstract class Game {

    public boolean running = false;

    private Window window;

    public void setWindow(Window window) {
        if(this.window != null)
            throw new IllegalStateException("window is already set");
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }

    public abstract void render(Graphics g);

    public abstract void update(float delta);

}
