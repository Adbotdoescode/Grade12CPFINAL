package final_project_2026;


/**
 * This class defines an extended record that the medic uses to determine the best possible zombie to heal. PLEASE NOTE, that the zombieInfoRecord will also contain survivors and the medic, however it will filter those out when sorting 
 * @author ayyan
 * @version June 12 2026
 */
public class ZombieInfoRecord extends RobotInfoRecord {
	
	// initialize instance variables
    private double distanceToMedic;
    private double undesirability = 0;
    private int dodges = 0;
    private int totalAttacks = 0;
    private double dodgeAbility = 0;
    
    
    /**
     * Constructor for initializing a ZombieInfoRecord
     * @param id - The id of the robot 
     * @param street - the street of the zombie
     * @param avenue - the avenue of the zombie
     * @param speed - the speed of the zombie
     * @param isZombie - weather or not the robot is a zombie
     * @param distanceToMedic - the distance from the medic to that robot
     */
    public ZombieInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, double distanceToMedic) {
        super(id, street, avenue, speed, isZombie, 0); 
        this.distanceToMedic = distanceToMedic;
    }
    
    /**
     * Getter method for returning the distance to the medic
     * @return - returns the distance to the medic
     */
    public double getDistanceToMedic() {
        return this.distanceToMedic;
    }
    
    
    /**
     * Setter method for totalUndesirability 
     * @param preferability - the you want to be set for totalUndesirability
     */
    public void setTotalUndesirability(double preferability) {
		this.undesirability = preferability;
	}
    
	
	/**
	 * Getter method for totalUndesirability
	 * @return - returns the double value for totalUndesirability
	 */
	public double getTotalUndesirability() { 
		return this.undesirability;
	}
	
	
	/**
	 * This method is used to increase dodges by 1
	 */
	public void increaseDodges () {
		this.dodges += 1;
	}
	
	/**
	 * Getter method for totalAttacks
	 * @return - returns the attribute totalAttacks
	 */
	public int getTotalAttacks () {
		return this.totalAttacks;
	}
	
	/**
	 * Increased the value of totalAttacks by 1
	 */
	public void increaseTotalAttacks () {
		this.totalAttacks += 1;
	}
	
	/**
	 * Getter method for the attribute dodges
	 * @return - returns the int value for dodges
	 */
	public int getTotalDodges () {
		return this.dodges;
	}
	
	/** getter method for the attribute dodgeAbility
	 * @return - returns the double value for dodgeAbility
	 */
	public double getDodgeAbility() {
		return this.dodgeAbility;
	}
	
	/**
	 * Setter method for the attribute dodgeAbility
	 * @param dodgeAbility - the double value that you want to be set for the attribute dodgeAbility
	 */
	public void setDodgeAbility(double dodgeAbility) { 
		this.dodgeAbility = dodgeAbility;
	}
	
	/** - setter method for the attribute dodges
	 * @param dodges - the int value that you want to be set for the attribute dodges
	 */
	public void setTotalDodges(int dodges) {
		this.dodges = dodges;
	}
	
	/** setter method for the attribute totalAttacks
	 * @param totalAttacks - the int value that you want to be set for the attribute totalAttacks
	 */
	public void setTotalAttacks(int totalAttacks) {
		this.totalAttacks = totalAttacks;
	}
}
