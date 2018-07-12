package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrain.Terrain;


public class Player extends Entity {

    private static final float RUN_SPEED = 50f;
    private static final float TURN_SPEED = 160f;
    public static final float GRAVITY = -50.0f;
    private static final float JUMP_POWER = 30f;

//    private static final float TERRAIN_HEIGHT = 0;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardSpeed = 0;

    private boolean isInAir = false;
    

    public Player(TexturedModel model, Vector3f position, float rotx, float roty, float rotz, float scale) {
        super(model, position, rotx, roty, rotz, scale);
    }

    public void move(Terrain terrain) {
        checkInputs();
        super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
        float dx = distance * (float) Math.sin(Math.toRadians(super.getRoty()));
        float dz = distance * (float) Math.cos(Math.toRadians(super.getRoty()));
        super.increasePosition(dx, 0, dz);
        upwardSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
        super.increasePosition(0, upwardSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        
        float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
        if (super.getPosition().y < terrainHeight) {
            upwardSpeed = 0;
            isInAir = false;
            super.getPosition().y = terrainHeight;
        }

    }

    private void jump() {
        if (!isInAir) {
            this.upwardSpeed = JUMP_POWER;
            isInAir = true;
        }
    }

    private void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            currentSpeed = RUN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            currentSpeed = -RUN_SPEED;
        } else {
            currentSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            currentTurnSpeed = -TURN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            currentTurnSpeed = TURN_SPEED;
        } else {
            currentTurnSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            jump();
        }

        // if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
        // position.y += speed;
        // }
        //
        // if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
        // position.y -= speed;
        // }
    }

}
