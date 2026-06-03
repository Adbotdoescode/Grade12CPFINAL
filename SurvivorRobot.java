package final_project_2026;

import becker.robots.*;

public class SurvivorRobot extends GameRobot {

	// Variables for survivor stats and inventory
	private int dodgeAbility;
	private int baseSpeed;
	private int maxCapacity;
	private int currentItems;

	/**
	 * Constructor to set up the survivor
	 * @param c : The city the robot is in
	 * @param st : Starting street
	 * @param ave : Starting avenue
	 * @param dir : Starting direction
	 * @param id : ID of the robot
	 * @param speed : The initial movement speed
	 * @param dodgeAbility : The stat used to evade zombie attacks
	 * @param maxCapacity : Maximum number of things the backpack can hold
	 */
	public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility, int maxCapacity) {
		// False at the end because the survivor is obviously not a zombie
		super(c, st, ave, dir, id, speed, false);
		this.dodgeAbility = dodgeAbility;
		this.baseSpeed = speed;
		this.maxCapacity = maxCapacity;
		this.currentItems = 0; 
	}

	/**
	 * The state machine that decides what action to take this turn
	 * @param state : The array of records representing everyone on the board
	 * @return TurnAction : The chosen action request sent to the app
	 */
	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		// First, organize the threats
		sortThreats(state);

		// Priority 1: Run away if a zombie is too close
		TurnAction evasionAction = evadeZombies(state);
		if (evasionAction != null) {
			return evasionAction; 
		}
		
		// Priority 2: Drop off things if the backpack is full
		if (this.currentItems >= this.maxCapacity) {
			return deliveryMode();
		}

		// Priority 3: Otherwise, just look for more things
		return forageMode();
	}

	/**
	 * Generates a record of this robot to share with others
	 * @return RobotInfoRecord
	 */
	@Override
	public RobotInfoRecord generateRecord() {
		// Speed goes down as more items are picked up
		int dynamicSpeed = Math.max(1, this.baseSpeed - this.currentItems);
		return new RobotInfoRecord(this.id, this.getStreet(), this.getAvenue(), dynamicSpeed, this.isZombie);
	}

	/**
	 * Returns the stat used for combat dice rolls
	 * @return int : dodge ability
	 */
	@Override
	public int getCombatAbility() {
		return this.dodgeAbility;
	}

	/**
	 * Sweeps the board in a lawnmower pattern to find things
	 * @return TurnAction : The move or pick up request
	 */
	private TurnAction forageMode() {
		// If standing on a thing, pick it up
		if (this.canPickThing()) {
			this.currentItems++; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.PICK_UP);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();

		// Move horizontally first
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
			// If facing North/South, figure out which wall we are at to sweep the other way
			if (this.getAvenue() >= 24) {
				plannedAvenue--; 
			} else {
				plannedAvenue++; 
			}
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	/**
	 * Finds the shortest path back to the safe zone at Street 1, Avenue 1
	 * @return TurnAction : The move or drop off request
	 */
	private TurnAction deliveryMode() {
		// Drop off everything if we reached the safe zone
		if (this.getStreet() == 1 && this.getAvenue() == 1) {
			this.currentItems = 0; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.DROP_OFF);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();
		int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);

		// Calculate L-shaped path
		for (int i = 0; i < availableSpeed; i++) {
			if (plannedAvenue > 1) { 
				plannedAvenue--; 
			} else if (plannedStreet > 1) { 
				plannedStreet--; 
			}
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	/**
	 * Calculates an escape route if a zombie enters the danger radius
	 * @param state : The sorted array of records
	 * @return TurnAction : The move request, or null if safe
	 */
	private TurnAction evadeZombies(RobotInfoRecord[] state) { 
		// Make sure there are actually zombies on the board
		if (state.length == 0 || !state[0].getIsZombie()) {
			return null;
		}

		int dangerRadius = 4;
		double distanceToClosest = calculateDistance(state[0].getStreet(), state[0].getAvenue());

		// Check if we need to run
		if (distanceToClosest <= dangerRadius) {
			int plannedStreet = this.getStreet();
			int plannedAvenue = this.getAvenue();
			int zombieStreet = state[0].getStreet();
			int zombieAvenue = state[0].getAvenue();

			int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);

			// Step away from the zombie, stopping at the fences
			for (int i = 0; i < availableSpeed; i++) {
				if (zombieAvenue >= plannedAvenue && plannedAvenue > 1) {
					plannedAvenue--;
				} else if (zombieAvenue <= plannedAvenue && plannedAvenue < 24) {
					plannedAvenue++;
				} else if (zombieStreet <= plannedStreet && plannedStreet < 13) {
					plannedStreet++;
				} else if (zombieStreet >= plannedStreet && plannedStreet > 1) {
					plannedStreet--;
				}
			}

			return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
		}
		
		return null;
	}

	/**
	 * Sorts the state array to put the most dangerous zombies at the front
	 * @param state : The array of records
	 */
	private void sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		
		// Selection sort
		for (int i = 0; i < n - 1; i++) {
			int mostDangerousIndex = i;
			
			for (int j = i + 1; j < n; j++) {
				if (state[j].getIsZombie()) {
					double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());
					// Threat score blends distance and speed
					double threatScoreJ = distanceJ / state[j].getSpeed();
					
					double currentMinThreatScore;
					if (state[mostDangerousIndex].getIsZombie()) {
						double currentDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
						currentMinThreatScore = currentDistance / state[mostDangerousIndex].getSpeed();
					} else {
						// Push non-zombies to the back by giving them a huge score
						currentMinThreatScore = Double.MAX_VALUE;
					}

					// Smallest threat score is the biggest danger
					if (threatScoreJ < currentMinThreatScore) {
						mostDangerousIndex = j;
					}
				}
			}
			
			// Swap
			if (mostDangerousIndex != i) {
				RobotInfoRecord temp = state[mostDangerousIndex];
				state[mostDangerousIndex] = state[i];
				state[i] = temp;
			}
		}
	}
}
