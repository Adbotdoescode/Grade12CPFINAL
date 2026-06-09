package final_project_2026;

import java.util.ArrayList;

import becker.robots.*;

public abstract class GameRobot extends RobotSE {

	// protected variables so the medic zombie and survivor can access them
	protected int id;
	protected int speed;
	protected boolean isZombie;
	protected int ability;

	public GameRobot(City c, int st, int ave, Direction dir, int id, int speed, boolean isZombie) {
		super(c, st, ave, dir);
		this.id = id;
		this.speed = speed;
		this.isZombie = isZombie;
	}
	public abstract TurnAction takeTurn(RobotInfoRecord[] state);

	public abstract TurnAction takeTurn(RobotInfoRecord[] state, ArrayList<ZombieInfoRecord> zombieRecords);

//	public abstract RobotInfoRecord generateRecord();

	protected double calculateDistance(int targetStreet, int targetAvenue) {
		int horizontalDistance = targetAvenue - this.getAvenue();
		int verticalDistance = targetStreet - this.getStreet();
		double distance = Math.sqrt((horizontalDistance * horizontalDistance) + (verticalDistance * verticalDistance));
		return distance; 
	}

	public int getId() {
		return this.id; 
	}
	public double getSpeed() { 
		return this.speed; 
	}
	public boolean isZombie() { 
		return this.isZombie; 
	}

	/**
	 * gets the combat stat of the robot for dice rolls
	 * @return int combat ability value
	 */
	public abstract int getCombatAbility();
	
	/**
     * gets the role of the robot for turn order and safe zone logic
     * @return string the role of the robot
     */
    public abstract String getRole();
    
}
