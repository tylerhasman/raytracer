package me.tyler.raytrace;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WorldRenderTask implements Runnable {

    private BufferedImage section;
    private int[] pixelData;

    private float cameraX, cameraY, cameraZ;
    private int sectionX, sectionY;

    private float fov;

    private float cameraYaw, cameraPitch;

    private float drawDistance;

    private World world;

    private float totalWidth, totalHeight;

    private RaytraceGame raytraceGame;

    public WorldRenderTask(int x, int y, int width, int height, float tw, float th, RaytraceGame raytraceGame){
        section = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixelData = new int[width * height];
        sectionX = x;
        sectionY = y;
        totalWidth = tw;
        totalHeight = th;
        this.raytraceGame = raytraceGame;
    }

    public int getSectionX() {
        return sectionX;
    }

    public int getSectionY() {
        return sectionY;
    }

    public BufferedImage getRenderedImage() {
        return section;
    }

    public void updateCamera(float x, float y, float z, float cameraYaw, float cameraPitch, float fov, float drawDistance, World world){
        cameraX = x;
        cameraZ = z;
        cameraY = y;
        this.fov = fov;
        this.cameraPitch = cameraPitch;
        this.cameraYaw = cameraYaw;
        this.drawDistance = drawDistance;
        this.world = world;
    }

    @Override
    public void run() {

        float horizontalInfluence = 1f; // FastMath.cos(FastMath.toRadians(0.5f * (fov / 2f) + cameraPitch));

        float startRayVelX = FastMath.cos(FastMath.toRadians(cameraYaw + fov * (0.5f - sectionX / totalWidth))) * horizontalInfluence;
        float finishRayVelX = FastMath.cos((FastMath.toRadians(cameraYaw + fov * (0.5f - (sectionX + section.getWidth() - 1) / totalWidth)))) * horizontalInfluence;

        float startRayVelZ = FastMath.sin(FastMath.toRadians(cameraYaw + fov * (0.5f - sectionX / totalWidth)))* horizontalInfluence;
        float finishRayVelZ = FastMath.sin((FastMath.toRadians(cameraYaw + fov * (0.5f - (sectionX + section.getWidth() - 1) / totalWidth))))* horizontalInfluence;

        float startRayVelY = FastMath.sin(FastMath.toRadians((0.5f - sectionY / totalHeight) * (fov / 2f) + cameraPitch));
        float finishRayVelY = FastMath.sin((FastMath.toRadians((0.5f - (sectionY + section.getHeight() - 1) / totalHeight) * (fov / 2f) + cameraPitch)));

        for(float i = sectionX; i < section.getWidth() + sectionX;i++){

            float rayScreenX = i / totalWidth;

            float rayVelX = rayScreenX * (finishRayVelX - startRayVelX) + startRayVelX;
            float rayVelZ = rayScreenX * (finishRayVelZ - startRayVelZ) + startRayVelZ;

            float rayWorldX = cameraX;
            float rayWorldZ = cameraZ;

            for(float j = sectionY; j < section.getHeight() + sectionY;j++){

                float rayScreenY = j / totalHeight;

                float rayWorldY = cameraY;

                float rayVelY = rayScreenY * (finishRayVelY - startRayVelY) + startRayVelY;

                int hitTile = 0;

                float dst = 0f;

                float hitX = 0, hitY = 0, hitZ = 0;

                for(float f = 0f; f < drawDistance;f += 0.025f){

                    hitX = rayVelX * f + rayWorldX;
                    hitY = rayVelY * f + rayWorldY;
                    hitZ = rayVelZ * f + rayWorldZ;

                    dst = f;

                    if(hitY >= 1f && rayWorldY < 1F){
                        hitTile = Tile.SKY.getId();
                        break;
                    }else if(hitY <= 0){
                        hitTile = Tile.GROUND.getId();
                        break;
                    }

                    int tile = world.getTileId((int)hitX, (int) hitZ);

                    if(tile != 0 && hitY > 0 && hitY < 1){
                        hitTile = Tile.BRICKS.getId();
                        break;
                    }

                }

                int r = 0, g = 0, b = 0;

                Tile tile = Tile.TILES[hitTile];

                BufferedImage image = raytraceGame.getTextures().get(tile.getTextureRef());

                if(image != null){

                    float xCoord = 0f;
                    float yCoord = 0f;

                    if(hitY <= 0F || hitY >= 1F){//Ground / Sky
                        xCoord = FastMath.fract(hitX);
                        yCoord = FastMath.fract(hitZ);
                    }else{//An actual tile

                        yCoord = FastMath.fract(hitY);
                        float fractX = FastMath.fract(hitX);
                        float fractZ = FastMath.fract(hitZ);

                        if(fractX <= 0.1f || fractX >= 0.9){
                            xCoord = fractZ;
                        }

                        if(fractZ <= 0.1f || fractZ >= 0.9f){
                            xCoord = fractX;
                        }

                    }

                    int rgb = image.getRGB((int) (xCoord * image.getWidth()), (int) (yCoord * image.getHeight()));

                    r = (rgb >> 16) & 0xFF;
                    g = (rgb >> 8) & 0xFF;
                    b = rgb & 0xFF;
                }

                r *= (1f - (dst / drawDistance)) * 0.7f;
                g *= (1f - (dst / drawDistance)) * 0.7f;
                b *= (1f - (dst / drawDistance)) * 0.7f;

                int packed = ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF));

                pixelData[(int) ((i - sectionX) + (j - sectionY) * section.getWidth())] = packed;
            }
        }
        section.getRaster().setDataElements(0, 0, section.getWidth(), section.getHeight(), pixelData);
    }

}
