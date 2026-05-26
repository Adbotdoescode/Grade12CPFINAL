package final_project_2026;

/**
 * Record class to store the data of each robot player
 * @author adam
 * @version May 24, 2026
 */
public class RobotInfoRecord {
	//instance variables to store robot data
	private int id;
	private int street; 
	private int avenue;
	private int speed;
	private int safeSpotAve;
	private int safeSpotStreet;
	private int itemsCarried;
	private boolean isZombie;
	
	//constructor to set data and intialize record
	public RobotInfoRecord(int id, int street, int avenue, int speed, boolean isZombie) {
		this.id = id;
        this.street = street;
        this.avenue = avenue;
        this.speed = speed;
        this.isZombie = isZombie;
	}
	
	//getter methods ----->
	 
	/**
	 * method to return the id of the robot
	 * @return int robot Id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * method to return the current street the robot is on
 	 * @return int robot street
	 */
	public int getStreet() {
		return this.street;
	}
	
	/**
	 * method to return the current avenue of the robot
	 * @return int robot avenue
	 */
	public int getAvenue() {
		return this.avenue;
	}
	
	/**
	 * method to return the speed of the robot
	 * @return int robot speed
	 */
	public int getSpeed() {
		return this.speed;
	}
	
	/**
	 * method to return the zombie status of the robot
	 * @return boolean zombie status
	 */
	public boolean getIsZombie() {
		return this.isZombie;
	}
	
	/**
	 * method to return amount of items this robot is carrying
	 * @return int amount of things
	 */
	public int getItemsCarried() {
		return this.itemsCarried;
	}
}
