package textures;

public class ModelTexture {
    private int textureID;
    private int normalMap;

    private float shineDamper = 1;
    private float reflectivity = 0;

    private boolean hasTransparency = false;
    private boolean useFakeLighting = false;
    
    private int numberOfRows = 1;

    public ModelTexture(int id) {
        this.textureID = id;
    }
    
    public int getID() {
        return this.textureID;
    }

    public boolean isUseFakeLighting() {
        return useFakeLighting;
    }
    
    public void setUseFakeLighting(boolean useFakeLighting) {
        this.useFakeLighting = useFakeLighting;
    }
    
    public void setHasTransparency(boolean transparency) {
        hasTransparency = transparency;
    }
    
    public boolean isHasTransparency() {
        return hasTransparency;
    }
    
    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }
    
    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }
    
    public int getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(int normalMap) {
        this.normalMap = normalMap;
    }
}