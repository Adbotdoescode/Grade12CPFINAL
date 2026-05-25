package final_project_2026;

import becker.robots.*;

public class SurvivorRobot extends GameRobot{
	
	// variable that determines dodging ability 
	private int dodgeAbility;
	
	/**
	 * Constructor for the SurvivorRobot
	 */
	public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility){
		
		// last one is "false" because the robot is not a zombie
		super(c, st, ave, dir, id, speed, false);
		
		// initialize the specific survivor attribute 
		this.dodgeAbility = dodgeAbility;
	}
	
	/**
	 * Method determined by GameRobot abstract class
	 * Blank for now as a stub
	 */
	@Override
	public void takeTurn(RobotInfoRecord[] state) {
		// adding logic later
	}
	
	// Additional stubs from UML diagram
	private void scanForThings() {
		// adding logic later
	}
	
	private void evadeZombies(RobotInfoRecord[] state) {
		// adding logic later
	}
	
	private void collectThing() {
		// adding logic later
	}

}
