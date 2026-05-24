package final_project_2026;
import java.awt.Color;
import becker.robots.*;

/**
 * main code for the medic robot
 * @author adam
 * @version May 24, 2026
 */
public class MedicRobot extends GameRobot{

	//medic attributes
	private int hitPoints;
	private int healingPower;
	private String currentStrategy;

	/**
	 * medic constructor, intializes it as a player within the game
	 * @param City city
	 * @param int intial street
	 * @param int intial avenue
	 * @param Direction intial direction
	 * @param int medic id
	 * @param int movement speed
	 */
	public MedicRobot(City c, int st, int ave, Direction dir, int id, int speed) {
		//calling parent constructor 
		super(c, st, ave, dir, id, speed, false); 
		this.hitPoints = 100;
		this.healingPower = 50;
		
		//starts by helping gather things
		this.currentStrategy = "GATHER"; 
		
		//changing robot color to white
		this.setColor(Color.WHITE);
	}

	
	/**
	 * The main logic of the medic robot
	 * @param Array of robot records for each robot state
	 */
	public void takeTurn(RobotInfoRecord[] state) {
		evaluateStrategy(state);

		if (currentStrategy.equals("HEAL")) {
			//logic to chase down zombies and heal them
		} else {
			//logic to move around and gather things
		}
	}
	
	/**
	 * Helper method to decide whcih state to be in
	 * @paramArray of robot records for each robot state
	 */
	private void evaluateStrategy(RobotInfoRecord[] state) {
		//count number of zombies vs survivors to decide what to do, potentially implement sorting here? 
	}


}
