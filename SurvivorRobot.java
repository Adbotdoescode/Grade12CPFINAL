package final_project_2026;

import becker.robots.*;

public class SurvivorRobot extends GameRobot {

	// these are the variables for the survivors stats and the stuff in their inventory
	private int dodgeAbility;
	private int baseSpeed;
	private int maxCapacity;
	private int currentItems;

	/**
	 * constructor setting up the survivor robot
	 * @param c : the city the robot is placed in
	 * @param st : the starting street
	 * @param ave : the starting avenue
	 * @param dir : starting direction
	 * @param id : id of the robot
	 * @param speed : the initial movement speed they start with
	 * @param dodgeAbility : the stat we use to evade zombie attacks
	 * @param maxCapacity : maximum number of things the backpack can hold at once
	 */
	public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility, int maxCapacity) {
		// put false at the end cause its obviously not a zombie
		super(c, st, ave, dir, id, speed, false);
		this.dodgeAbility = dodgeAbility;
		this.baseSpeed = speed;
		this.maxCapacity = maxCapacity;
		this.currentItems = 0; 
	}

	/**
	 * this is the state machine that figures out what action to take this turn
	 * @param state : the array of records representing everyone else on the board
	 * @return TurnAction : the chosen action request that gets sent to the app
	 */
	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		// gotta sort the threats first so we know whose closest
		sortThreats(state);

		// priority 1 is running away if a zombie gets too close to us
		TurnAction evasionAction = evadeZombies(state);
		if (evasionAction != null) {
			return evasionAction; 
		}
		
		// priority 2 is dropping the things off if our backpack gets completely full
		if (this.currentItems >= this.maxCapacity) {
			return deliveryMode();
		}

		// priority 3 is just looking around for more things if we are safe
		return forageMode();
	}


	/**
	 * returns the stat used for the combat dice rolls
	 * @return int : dodge ability
	 */
	@Override
	public int getCombatAbility() {
		return this.dodgeAbility;
	}

	/**
	 * sweeps the board in a lawnmower pattern so we can find things
	 * @return TurnAction : the move or pick up request
	 */
	private TurnAction forageMode() {
		// if we are right on top of a thing we might as well pick it up
		if (this.canPickThing()) {
			this.currentItems++; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.PICK_UP);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();

		// moving horizontally across first
		if (this.getDirection() == Direction.EAST) {
			if (this.getAvenue() < 24) { 
				plannedAvenue++; 
			} else if (this.getStreet() < 13) { 
				plannedStreet++; 
			} else {
				// hit the bottom right corner so we just gotta go west
				plannedAvenue--; 
			}
		} else if (this.getDirection() == Direction.WEST) {
			if (this.getAvenue() > 1) { 
				plannedAvenue--; 
			} else if (this.getStreet() < 13) { 
				plannedStreet++; 
			} else {
				// hit the bottom left corner so we go east now
				plannedAvenue++; 
			}
		} else {
			// if facing north or south we just gotta figure out what wall we are at so we can sweep the other direction
			if (this.getAvenue() >= 24) {
				plannedAvenue--; 
			} else {
				plannedAvenue++; 
			}
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	/**
	 * finds the shortest path back to the safe zone at street 1 avenue 1
	 * @return TurnAction : the move or drop off request
	 */
	private TurnAction deliveryMode() {
		// drop off everything once we finally reach the safe zone
		if (this.getStreet() == 1 && this.getAvenue() == 1) {
			this.currentItems = 0; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.DROP_OFF);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();
		int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);

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
	 * calculates an escape route if a zombie enters our danger radius
	 * @param state : the sorted array of records
	 * @return TurnAction : the move request, or null if were safe
	 */
	private TurnAction evadeZombies(RobotInfoRecord[] state) { 
		// gotta make sure there are actually zombies on the board first
		if (state.length == 0 || !state[0].getIsZombie()) {
			return null;
		}

		int dangerRadius = 4;
		double distanceToClosest = calculateDistance(state[0].getStreet(), state[0].getAvenue());

		if (distanceToClosest <= dangerRadius) {
			int plannedStreet = this.getStreet();
			int plannedAvenue = this.getAvenue();
			int zombieStreet = state[0].getStreet();
			int zombieAvenue = state[0].getAvenue();

			int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);

			// stepping away from the zombie but making sure we stop at the fences
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
	 * sorts the state array so we put the most dangerous zombies right at the front
	 * @param state : the array of records
	 */
	private void sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		
		for (int i = 0; i < n - 1; i++) {
			int mostDangerousIndex = i;
			
			for (int j = i + 1; j < n; j++) {
				if (state[j].getIsZombie()) {
					double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());
					
					// this threat score blends the distance and speed together so we know the real danger
					double threatScoreJ = distanceJ / state[j].getSpeed();
					
					double currentMinThreatScore;
					if (state[mostDangerousIndex].getIsZombie()) {
						double currentDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
						currentMinThreatScore = currentDistance / state[mostDangerousIndex].getSpeed();
					} else {
						// giving non zombies a huge score so they get pushed to the back
						currentMinThreatScore = Double.MAX_VALUE;
					}

					if (threatScoreJ < currentMinThreatScore) {
						mostDangerousIndex = j;
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

	@Override
	public String getRole() {
		// returning null for the role for right now
		return null;
	}
}