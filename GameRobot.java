package final_project_2026;

import becker.robots.*;

public abstract class GameRobot extends RobotSE{
	
	//protected variables, so the medic, zombie, and survivor can accsess them
	protected int id;
	protected int speed;
	protected boolean isZombie;
	
	/**
	 * constructor, taking in additional values
	 * @param City
	 * @param int initial street
	 * @param int initial avenue
	 * @param Direction initial direction
	 * @param int id of robot
	 * @param int speed of robot
	 * @param boolean intial szombie status of the robot
	 */
	public GameRobot(City c, int st, int ave, Direction dir, int id, int speed, boolean isZombie) {
        super(c, st, ave, dir);
        this.id = id;
        this.speed = speed;
        this.isZombie = isZombie;
    }
	
	/**
     * the main action method that dictates what this robot does on its turn
     * every child class (ZombieRobot, SurvivorRobot, MedicRobot) MUST override 
     * this method to implement its own unique AI and movement strategy
     * * @param state An array of RobotInfoRecord objects representing the current 
     * locations, speeds, and infection statuses of all players on the grid
     */
	public abstract void takeTurn(RobotInfoRecord[] state);
	
	/**
     * shared utility method to calculate the distance between this robot's 
     * current location and a specific target location
     * child classes can call this to evaluate targets
     * * @param int  the street of the target location.
     * @param int the avenue of the target location.
     * @return the calculated distance to the target as a double
     */
    protected double calculateDistance(int targetStreet, int targetAvenue) {
    	
    		// Find the difference between the target coordinates and the robot's current coordinates 
    		double streetDiff = targetStreet - this.getStreet();
    		double avenueDiff = targetAvenue - this.getAvenue();
    		
    		// Apply the Pythagorean theorem 
    		return Math.sqrt(Math.pow(streetDiff, 2) + Math.pow(avenueDiff, 2));
    }
}
