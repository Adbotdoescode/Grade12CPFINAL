package final_project_2026;

public class RobotInfoRecord {
    // instance variables to store robot data
    private int id;
    private int street; 
    private int avenue;
    private int speed;
    private int safeSpotAve;
    private int safeSpotStreet;
    private int itemsCarried;
    private boolean isZombie;
    
    // constructor to set data and intialize record
    public RobotInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, int itemsCarried) {
        this.id = id;
        this.street = street;
        this.avenue = avenue;
        this.speed = speed;
        this.isZombie = isZombie;
        this.itemsCarried = itemsCarried;
    }
    

	public int getId() {
        return this.id;
    }
    
    public int getStreet() {
        return this.street;
    }
    
    public int getAvenue() {
        return this.avenue;
    }
    
    public int getSpeed() {
        return this.speed;
    }
    
    public boolean getIsZombie() {
        return this.isZombie;
    }
    
    public int getItemsCarried() {
        return this.itemsCarried;
    }
}