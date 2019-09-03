package me.tyler.raytrace;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RaytraceGame extends Game{

    private World world;

    private float cameraX, cameraY, cameraZ, cameraYaw, cameraPitch;
    private float fov;
    private float drawDistance;

    private BufferedImage cameraBuffer;

    private Map<String, BufferedImage> textures;

    private ForkJoinPool forkJoinPool;

    private ArrayList<WorldRenderTask> worldRenderTasks;

    private Light cameraLight;

    public RaytraceGame(){
        textures = new HashMap<>();

        try {
            textures.put("dirt", ImageIO.read(new File("assets/dirt.png")));
            textures.put("bricks", ImageIO.read(new File("assets/bricks.png")));
            textures.put("map", ImageIO.read(new File("assets/map.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage map = textures.get("map");

        world = new World(map.getWidth(), map.getHeight());

        for(int i = 0; i < map.getWidth();i++){
            for(int j = 0; j < map.getHeight();j++){

                int rgb = map.getRGB(i, j) & 0xFFFFFF;
                Color color = new Color(rgb);

                if(rgb == 0){
                    world.setTileId(i, j, Tile.BRICKS.getId());
                }else if(rgb == 0xFFFFFF) {
                    world.setTileId(i, j, Tile.AIR.getId());
                }else if(color.getRed() == 255 && color.getBlue() == 0 && color.getGreen() == 0){
                    world.addObject(new CubeObject(i, 0, j, rgb, 1f, 0.5f, 1f));
                }else{
                    world.addLight(new Light(i + 0.5f, j + 0.5f, 30f, 2f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
                }

            }
        }

        world.addLight(cameraLight = new Light(0f, 0f, 30f, 3f, 1f, 1f, 1f));
        cameraX = 2.5f;
        cameraY = 0.5f;
        cameraZ = 2.5f;
        cameraBuffer = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        drawDistance = 10f;
        cameraYaw = 0;//degrees
        cameraPitch = 0;//degrees
        fov = 90f;

        int widthThreads = 2;
        int heightThreads = 2;
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        worldRenderTasks = new ArrayList<>();
        for(int i = 0; i < widthThreads;i++){
            for(int j = 0; j < heightThreads;j++){
                int w = cameraBuffer.getWidth() / widthThreads;
                int h = cameraBuffer.getHeight() / heightThreads;
                worldRenderTasks.add(new WorldRenderTask(i * w, j * h, w, h, cameraBuffer.getWidth(), cameraBuffer.getHeight(), this));
            }
        }
    }

    public Map<String, BufferedImage> getTextures() {
        return textures;
    }

    private void renderCamera(){
        Graphics g = cameraBuffer.getGraphics();
        g.setColor(Color.BLACK);
        //g.fillRect(0, 0, cameraBuffer.getWidth(), cameraBuffer.getHeight());

        for(WorldRenderTask worldRenderTask : worldRenderTasks){
            worldRenderTask.updateCamera(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch, fov, drawDistance, world);
            forkJoinPool.execute(worldRenderTask);
        }

        forkJoinPool.awaitQuiescence(1000, TimeUnit.MILLISECONDS);

        for(WorldRenderTask worldRenderTask : worldRenderTasks){
            g.drawImage(worldRenderTask.getRenderedImage(), worldRenderTask.getSectionX(), worldRenderTask.getSectionY(), null);
        }

    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);

        renderCamera();

        g.drawImage(cameraBuffer, 0, 0, getWindow().getWidth(), getWindow().getHeight(), null);

        g.drawImage(textures.get("map"), 0, 60, 128, 128, null);

        g.setColor(Color.RED);
        g.fillOval((int) cameraX * 4, (int) (60 + cameraZ * 4), 4, 4);

        g.setColor(Color.WHITE);
        g.drawString(cameraX+"/"+cameraZ, 5, 30);
        g.drawString(cameraYaw+"/"+cameraPitch, 5, 45);
    }

    @Override
    public void update(float delta) {

        float velX = 0, velZ = 0;

        if(Input.isPressed(KeyEvent.VK_W)){

            float dirX = (float) Math.cos(Math.toRadians(cameraYaw));
            float dirZ = (float) Math.sin(Math.toRadians(cameraYaw));

            velX += dirX * delta * 3;
            velZ += dirZ * delta * 3;
        }else if(Input.isPressed(KeyEvent.VK_S)){

            float dirX = (float) Math.cos(Math.toRadians(cameraYaw));
            float dirZ = (float) Math.sin(Math.toRadians(cameraYaw));

            velX -= dirX * delta * 2;
            velZ -= dirZ * delta * 2;
        }

        if(Input.isPressed(KeyEvent.VK_A)){

            float dirX = (float) Math.cos(Math.toRadians(cameraYaw - 90));
            float dirZ = (float) Math.sin(Math.toRadians(cameraYaw - 90));

            velX -= dirX * delta * 2;
            velZ -= dirZ * delta * 2;
        }else if(Input.isPressed(KeyEvent.VK_D)){
            float dirX = (float) Math.cos(Math.toRadians(cameraYaw - 90));
            float dirZ = (float) Math.sin(Math.toRadians(cameraYaw - 90));

            velX += dirX * delta * 2;
            velZ += dirZ * delta * 2;
        }

        float checkX = cameraX + velX;
        float checkZ = cameraZ + velZ;

        if(world.getTileId((int) checkX, (int) cameraZ) == 0){
            cameraX = checkX;
        }

        if(world.getTileId((int)cameraX, (int) checkZ) == 0){
            cameraZ = checkZ;
        }

        /*if(Input.isPressed(KeyEvent.VK_SPACE)){
            world.addObject(new Bullet(cameraX, cameraZ, cameraYaw));
        }*/

        float deltaX = Input.getDeltaX();
        float deltaY = Input.getDeltaY();

        cameraPitch += deltaY / 6f;
        cameraYaw += deltaX / 6f;

        if(cameraPitch <= -60)
            cameraPitch = -60;
        if(cameraPitch > 60)
            cameraPitch = 60;

        cameraLight.x = cameraX;
        cameraLight.z = cameraZ;

        world.update(delta);
    }
}
