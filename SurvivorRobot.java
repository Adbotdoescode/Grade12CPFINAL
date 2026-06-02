package g12CP_FinalProject;

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

		// Sort the threats so the most dangerous zombies are at index 0
		sortThreats(state);

		// Panic Mode (run away from zombie)
		// Check if the survivor needs to run away from a nearby zombie
		TurnAction evasionAction = evadeZombies1(state);
		if (evasionAction != null) {
			return evasionAction; // If a threat is within the danger radius, run
		}
		
		// Delivery Mode (backpack is full)
		// If backpack is completely full, go back to Safe Zone to drop off eveyrthing
		if (this.currentItems >= this.maxCapacity) {
			return deliveryMode();
		}

		// If survivor is safe and have room in our backpack, sweep the grid for items
		return forageMode();
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
 * Calculates the fastest path to the Safe Zone in top left (Street 1, Avenue 1)
 * Called only when the backpack is full of things
 * @return A TurnAction request for the application class
 */
private TurnAction deliveryMode() {

	// Check if the survivor is already in the Safe zone
	if (this.getStreet() == 1 && this.getAvenue() == 1) {
		return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.DROP_OFF);
	}

	// Otherwise, calcualte the next step in the L-shape path to go back to the Safe Zone
	int plannedStreet = this.getStreet();
	int plannedAvenue = this.getAvenue();

	// Calculate current speed (baseSpeed - currentItems)
	int availableSpeed = this.baseSpeed = this.currentItems;

	// Plan one step at a time until the avialable speed is reached, because that's the amount of steps the survivor can move at once
	for (int i = 0; i < availableSpeed; i++) {

		// Move west towards Avenue 1 first
		if (plannedAvenue > 1) {
			plannedAvenue--;
		}

		// Move north towards street 1
		if (plannedStreet > 1) {
			plannedStreet--;
		}

	}

	// Send the coordinates to move to Safe Zone
	return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
}

/**
 * Evaluates the most dangerous zombie and calculates a escape path in the opposite direction if it gets too close
 * @param state The sorted array of RoboInfoRecord
 * @return A TurnAction request to run away, or nothing if there is no zombie within range
 */
private TurnAction evadeZombies1(RobotInfoRecord[] state) {

	// See if there is actually a zombie on the field
	if (state.length == 0 || !state[0].getIsZombie()) {
		return null;
	}

	// Check if the most dangerous zombie is inside the survivors danger radius 
	int dangerRadius = 4;
	double distanceToClosest = calculateDistance(state[0].getStreet(), state[0].getAvenue());

	if (distanceToClosest <= dangerRadius) {
		// Calculate the path to ecsape
		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();
		int zombieStreet = state[0].getStreet();
		int zombieAvenue = state[0].getAvenue();

		// Calculate current limit on speed
		int availableSpeed = Math.max(1,  this.baseSpeed - this.currentItems);

		// Plan one step at a time to move away from zombie
		for (int i = 0; i < availableSpeed; i++) {

			// If zombie is east, run west but stop at the west wall
			if (zombieAvenue >= plannedAvenue && plannedAvenue < 24) {
				plannedAvenue++;
			}

			// If aligned on avenues or right beside a wall, use the streets to run away
			else if (zombieStreet >= plannedStreet && plannedStreet > 1) {
				plannedStreet--;
			}

			// If zombie is north, go south, but stop at south wall
			else if (zombieStreet <= plannedStreet && plannedStreet < 13) {
				plannedStreet++;
			}
		}

		// Send the escape coordinates to the main 
		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	// If zombie isn't close enough, return null and keep foraging
	return null;
}

/**
 * Sorts the array of robot records using Selection Sort
 * It prioritizes the most dangerous zombies by calculating the number of steps they need to reach the survivor
 * @param state The array of RobotInfoRecord 
 */
private void sortThreats(RobotInfoRecord[] state) {
	int n = state.length;

	// Loop through the entire array 
	for (int i = 0; i < n - 1; i++) {

		// Assume the current index is the most dangerous zombie with the smallest threat score (Smaller threat score means higher danger)
		int mostDangerousIndex = i;

		// Check the rest of the array to see if there is a bigger threat (smaller threat score)
		for (int j = i + 1; j < n; j++) {

			// Make sure that the robot being looked at is a zombie
			if (state[j].getIsZombie()) {

				// Calculate the distance
				double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());

				// Calculate the Threat Score (Threat score = distance / speed)
				double threatScoreJ = distanceJ / state[j].getSpeed();

				// Get the threat score of the current "most dangerous" index
				double currentMinThreatScore;
				if (state[mostDangerousIndex].getIsZombie()) {
					double currentDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
					currentMinThreatScore = currentDistance / state[mostDangerousIndex].getSpeed();
				} else {
					// If the current most dangerous isn't a zombie, give it a really big score which means no danger so the actual zombie takes priority
					currentMinThreatScore = Double.MAX_VALUE;
				}

				// Check if the new zombie has a lower threat score (meaning it will reach the survivor in less steps)
				if (threatScoreJ < currentMinThreatScore) {
					mostDangerousIndex = j;
				}
			}
		}

		// Swap the most dangerous zombie to the front of the unsorted part of the array
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
