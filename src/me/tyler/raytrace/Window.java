package me.tyler.raytrace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Window extends Canvas implements Runnable {

    private JFrame frame;

    private BufferedImage buffer;

    private Game game;

    private int targetFPS;

    private int fps;

    private boolean captureMouse;
    private boolean releasingMouse;

    private Cursor blankCursor;

    public Window(Game game, int width, int height, String title) {
        game.setWindow(this);
        setSize(width, height);
        frame = new JFrame(title);
        frame.setSize(new Dimension(width, height));
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.game = game;
        targetFPS = 30;
        captureMouse = true;
        releasingMouse = true;
        addKeyListener(Input.instance());
        addMouseListener(Input.instance());

        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    }

    private void clearBuffer(){
        Graphics g = buffer.getGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0,0, buffer.getWidth(), buffer.getHeight());
        g.setColor(Color.WHITE);
    }

    private void doMouseCapture() {
        if(captureMouse){

            if(releasingMouse){
                if(Input.isMouseDown()){
                    releasingMouse = false;
                    frame.setCursor(blankCursor);
                }
            }else{
                if(Input.isPressed(KeyEvent.VK_ESCAPE)){
                    releasingMouse = true;
                    frame.setCursor(Cursor.getDefaultCursor());
                }
            }

            if(!releasingMouse){
                Input.captureMouse(frame.getX() + frame.getWidth() / 2, frame.getY() + frame.getHeight() / 2);
            }

        }
    }

    @Override
    public void run(){
        game.running = true;
        long lastUpdate = System.currentTimeMillis();

        int fps = 0;
        float fpsCounter = 1f;

        try{
            while(game.running && frame.isVisible()){
                float delta = (System.currentTimeMillis() - lastUpdate) / 1000F;
                lastUpdate = System.currentTimeMillis();

                fpsCounter -= delta;
                if(fpsCounter <= 0) {
                    fpsCounter = 1f;
                    this.fps = fps;
                    fps = 0;
                }
                fps++;

                doMouseCapture();

                game.update(delta);
                clearBuffer();

                game.render(buffer.getGraphics());

                buffer.getGraphics().drawString("FPS: "+this.fps, 5, 20);

                getGraphics().drawImage(buffer, 0, 0, null);

                long sleepTime = (1000 / targetFPS) - (System.currentTimeMillis() - lastUpdate);

                if(sleepTime > 0){
                    Thread.sleep(sleepTime);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        frame.dispose();

    }


}
