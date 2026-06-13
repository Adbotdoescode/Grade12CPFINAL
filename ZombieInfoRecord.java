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
    
	
	public double getTotalUndesirability() { 
		return this.undesirability;
	}
	
	public void increaseDodges () {
		this.dodges += 1;
	}
	
	public int getTotalAttacks () {
		return this.totalAttacks;
	}
	
	public void increaseTotalAttacks () {
		this.totalAttacks += 1;
	}
	
	public int getTotalDodges () {
		return this.dodges;
	}
	
	public double getDodgeAbility() {
		return this.dodgeAbility;
	}
	
	public void setDodgeAbility(double dodgeAbility) { 
		this.dodgeAbility = dodgeAbility;
	}
	
	public void setTotalDodges(int dodges) {
		this.dodges = dodges;
	}
	
	public void setTotalAttacks(int totalAttacks) {
		this.totalAttacks = totalAttacks;
	}
}
