package me.tyler.raytrace;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

public class Input implements KeyListener, MouseListener {

    private static final Input input = new Input();

    static Input instance() {
        return input;
    }

    private Map<Integer, Boolean> keys;

    private boolean mouseDown;

    private float deltaX, deltaY;

    private Robot robot;

    private Input(){
        keys = new HashMap<>();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static float getDeltaX(){
        return input.deltaX;
    }

    public static float getDeltaY(){
        return input.deltaY;
    }

    public static boolean isPressed(int keyCode){
        return input.keys.getOrDefault(keyCode, false);
    }

    public static boolean isMouseDown(){
        return input.mouseDown;
    }

    public static void captureMouse(int x, int y){
        Robot robot = input.robot;
        if(robot != null){
            Point point = getMouse();
            input.deltaX = x - point.x;
            input.deltaY = y - point.y;
            robot.mouseMove(x, y);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.put(e.getKeyCode(), false);
    }

    public static Point getMouse(){
        return MouseInfo.getPointerInfo().getLocation();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
