package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;

public class Entity {

    private TexturedModel model;
    private Vector3f position;
    private float rotX, rotY, rotZ;
    private float scale;
    
    private int textureIndex = 0;
    
    public Entity(TexturedModel model, Vector3f position, float rotx, float roty, float rotz, float scale) {
        super();
        this.model = model;
        this.position = position;
        this.rotX = rotx;
        this.rotY = roty;
        this.rotZ = rotz;
        this.scale = scale;
    }
    
    public Entity(TexturedModel model, int index, Vector3f position, float rotx, float roty, float rotz, float scale) {
        super();
        this.model = model;
        this.setTextureIndex(index);
        this.position = position;
        this.rotX = rotx;
        this.rotY = roty;
        this.rotZ = rotz;
        this.scale = scale;
    }
    
    public void increasePosition(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
        
    }
    
    public void increaseRotation(float dx, float dy, float dz) {
        this.rotX += dx;
        this.rotY += dy;
        this.rotZ += dz;
        
    }
    
    public void setTextureIndex(int index) {
        this.textureIndex = index;
    }
    
    public float getTextureXOffset () {
        int col = textureIndex % model.getTexture().getNumberOfRows();
        return (float)col/(float)model.getTexture().getNumberOfRows();
    }
    
    public float getTextureYOffset () {
        int row = textureIndex / model.getTexture().getNumberOfRows();
        return (float)row/(float)model.getTexture().getNumberOfRows();
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRotx() {
        return rotX;
    }

    public void setRotx(float rotx) {
        this.rotX = rotx;
    }

    public float getRoty() {
        return rotY;
    }

    public void setRoty(float roty) {
        this.rotY = roty;
    }

    public float getRotz() {
        return rotZ;
    }

    public void setRotz(float rotz) {
        this.rotZ = rotz;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
 
}
