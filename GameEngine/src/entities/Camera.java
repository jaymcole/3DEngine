package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
    
    private float distanceFromPlayer = 50;
    private float angleAroundPlayer = 0;
    
    public Vector3f position = new Vector3f(0,0,0);
    private float pitch;
    private float yaw;
    private float roll;
    private float speed = 0.1f;
    
    private Player player;
    private Vector3f viewTarget = new Vector3f();
    
    public Camera(Player player) {
        this.player = player;
    }

    public void move() {
        calculateZoom();
        calculatePitch();
        calculateAngleAroundPlayer();
        
        viewTarget.x = player.getPosition().x;
        viewTarget.y = player.getPosition().y + 5;
        viewTarget.z = player.getPosition().z;

        float horizontalDistance = calculateHorizontalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (player.getRoty()) + angleAroundPlayer;
        
    }

    public Vector3f getPosition() {
        return position;
    }


    public float getPitch() {
        return pitch;
    }

    public void invertPitch() {
        this.pitch = -pitch;
    }

    public float getYaw() {
        return yaw;
    }


    public float getRoll() {
        return roll;
    }
    
    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        float theta = player.getRoty() + angleAroundPlayer;
        float offsetX = horizontalDistance * (float)Math.sin(Math.toRadians(theta));
        float offsetZ = horizontalDistance * (float)Math.cos(Math.toRadians(theta));
        position.x = viewTarget.x - offsetX;
        position.y = viewTarget.y + verticalDistance;
        position.z = viewTarget.z - offsetZ;
    }
    
    private float calculateHorizontalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
    }
    
    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
    }
    
    private void calculateZoom() {
        float zoomLevel = Mouse.getDWheel() * 0.1f;
        distanceFromPlayer -= zoomLevel;
    }
    
    private void calculatePitch() {
        if (Mouse.isButtonDown(1)) {
            float pitchChange = Mouse.getDY() * 0.1f;
            pitch -= pitchChange;
        }
    }
    
    private void calculateAngleAroundPlayer() {
//        if (Mouse.isButtonDown(0)) {
//            float angleChange = Mouse.getDX() * 0.3f;
//            angleAroundPlayer -= angleChange;
//        }
    }
    
}
