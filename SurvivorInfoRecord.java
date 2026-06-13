package final_project_2026;

/**
 * Represents a record containing information about a survivor robot
 * Extends the RobotInfoRecord class with additional survivor-specific fields
 * @author Adam
 * @version june 12, 2026
 */
public class SurvivorInfoRecord extends RobotInfoRecord {
    
    private int estimatedEvade;    
    private int distanceToMedic;  

    /**
     * Constructs a SurvivorInfoRecord with specified details
     * @param id The unique identifier for the robot
     * @param street The street location oa the robot
     * @param avenue The avenue location of the robot
     * @param speed The movement speed of the robot
     * @param isZombie Indicates if the robot is a zombie
     * @param itemsCarried The number of items the robot is carrying
     * @param estimatedEvade The estimated evasion capability of the survivor
     * @param distanceToMedic The distance to the nearest medic
     */    
    public SurvivorInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, int itemsCarried, int estimatedEvade, int distanceToMedic) {
        // Add a 0 at the end to correctly pass itemsCarried and 0 for dodges
        super(id, street, avenue, speed, isZombie, itemsCarried, 0); 
        this.estimatedEvade = estimatedEvade;
        this.distanceToMedic = distanceToMedic;
    }

    /**
     * Returns the estimated evasion capability of the survivor
     * @return The estimated evasion capability
     */
    public int getEstimatedEvade() {
        return this.estimatedEvade;
    }

    /**
     * Returns the distance to the nearest medic
     * @return The distance to the nearest medic
     */
    public int getDistanceToMedic() {
        return this.distanceToMedic;
    }
}
