package final_project_2026;

import becker.robots.*;

public class SurvivorRobot extends GameRobot {
	
	// Variable that determines the survivor's ability to dodge zombies
	private int dodgeAbility;
	
	// Variables for speed and capacity
	private int baseSpeed;
	private int maxCapacity;
	private int currentItems;
	
	/**
	 * Constructor
	 * @param c : The city the robot is in
	 * @param st : Starting street
	 * @param ave : Starting avenue
	 * @param dir : Starting direction
	 * @param id : ID of the robot
	 * @param speed : The movement speed of the robot
	 * @param dodgeAbility : The ability to dodge zombies
	 * @param maxCapacity : Maximum number of things the backpack can hold
	 */
	public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility, int maxCapacity) {
		
		// Last one is false because the robot not a zombie
		super(c, st, ave, dir, id, speed, false);
		
		// Initialize the specific survivor attributes 
		this.dodgeAbility = dodgeAbility;
		this.baseSpeed = speed;
		this.maxCapacity = maxCapacity;
		this.currentItems = 0; 
	}
	
	/**
	 * Method determined by GameRobot abstrac class
	 * It raeds the city and returns a request to the OutbreakApp
	 */
	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		
		// Just to test things out - move 2 spaces forward on the avenue
		int requestedSpaces = 2;
		int targetStreet = this.getStreet();
		int targetAvenue = this.getAvenue() + requestedSpaces;
		
		// Put the target lication and the intent into the new record to return to the OutbreakaAPp
		return new TurnAction(targetStreet, targetAvenue, TurnAction.MOVE);
	}
	
	/**
	 * The method with the actual actions that is called only if the aplpication class approves
	 * @param spaces Number of spaces the OutbreakApp approved to move
	 */
	public void executeApprovedMove(int spaces) {
		for (int i = 0; i < spaces; i++) {
			this.move();
		}
	}
	
	/**
	 * Lawnmower sweeps the entire field using a lawnmower pattern to find things
	 * @return A TurnAction request for the application class
	 */
	private TurnAction forageMode() {
		
		// Sensor - Check if the survivor is right on a thing
		if (this.canPickThing()) {
			// A thing was found, so request to pick it up
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.PICK_UP);
		}

		// Sweep - Determine the next step in the lawnmower pattern
		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();

		// Check which way currently facing to continue sweeping
		if (this.getDirection() == Direction.EAST) {
			if (this.getAvenue() < 24) {
				plannedAvenue++; 
			} else {
				plannedStreet++; 
			}
		} else if (this.getDirection() == Direction.WEST) {
			if (this.getAvenue() > 1) {
				plannedAvenue--; 
			} else {
				plannedStreet++; 
			}
		} else {
			// If robot is facing North or South, default East to start a sweep
			plannedAvenue++; 
		}

		// Request - Send the coordinates for the next step in the sweep
		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}
	
	/**
	 * Sorts the array of robot records using Selection Sort
	 * It prioritizes the closest zombies based on their distance and speed
	 * @param state The array of RobotInfoRecord 
	 */
	private void sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		
		// Loop through the entire array 
		for (int i = 0; i < n - 1; i++) {
			
			// Assume the current index is the most dangerous one with the smallest distance away
			int mostDangerousIndex = i;
			
			// Check the rest of the unsorted array to see if there is bigger threat
			for (int j = i + 1; j < n; j++) {
				
				// Make sure that the robot being looked at is a zombie (because no need to run from medics or other survivors)
				if (state[j].getIsZombie()) {
					
					// Get the distance from the survivor to the zombie
					double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());
					
					// If the current "most dangerous" isn't a zombie, give it an extremely large distance so the actual zombie takes priority
					double currentMinDistance;
					if (state[mostDangerousIndex].getIsZombie()) {
						currentMinDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
					} else {
						currentMinDistance = Double.MAX_VALUE;
					}
					
					// Check if the new zombie is closer than the current closest
					if (distanceJ < currentMinDistance) {
						mostDangerousIndex = j;
					}
					// If two zombies are the same distance away, the faster one is a bigger threat
					else if (distanceJ == currentMinDistance) {
						if (state[j].getSpeed() > state[mostDangerousIndex].getSpeed()) {
							mostDangerousIndex = j;
						}
					}
				}
			}
			
			if (mostDangerousIndex != i) {
				RobotInfoRecord temp = state[mostDangerousIndex];
				state[mostDangerousIndex] = state[i];
				state[i] = temp;
			}
		}
	}
	
	private void scanForThings() {
		// Doing later
	}
	
	private void evadeZombies(RobotInfoRecord[] state) {
		// Doing later
	}
	
	private void collectThing() {
		// Doing later
	}
}
