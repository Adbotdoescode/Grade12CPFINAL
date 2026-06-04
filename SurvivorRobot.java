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
		super(c, st, ave, dir, id, speed, false);
		this.dodgeAbility = dodgeAbility;
		this.baseSpeed = speed;
		this.maxCapacity = maxCapacity;
		this.currentItems = 0; 
	}

	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		sortThreats(state);

		// Priority 1: Run away if a zombie is too close
		TurnAction evasionAction = evadeZombies(state);
		if (evasionAction != null) {
			return evasionAction; 
		}
		
		// Priority 2: Drop off things if the backpack is full
		// NOTE: Your engine counts items immediately on pick up. 
		// We empty the inventory virtually so they don't lose all their speed, 
		// but we skip forcing them to walk to (1,1) since it's a death trap!
		if (this.currentItems >= this.maxCapacity) {
			this.currentItems = 0; 
			// Return a WAIT turn to simulate taking a turn to empty the backpack
			return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
		}

		// Priority 3: Otherwise, just look for more things
		return forageMode();
	}

	@Override
	public RobotInfoRecord generateRecord() {
		// Speed goes down as more items are picked up
		int dynamicSpeed = Math.max(1, this.baseSpeed - this.currentItems);
		return new RobotInfoRecord(this.id, this.getStreet(), this.getAvenue(), dynamicSpeed, this.isZombie, this.currentItems);
	}

	@Override
	public int getCombatAbility() {
		return this.dodgeAbility;
	}

	private TurnAction forageMode() {
		if (this.canPickThing()) {
			this.currentItems++; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.PICK_UP);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();

		if (this.getDirection() == Direction.EAST) {
			if (this.getAvenue() < 24) { plannedAvenue++; } 
			else { plannedStreet++; }
		} else if (this.getDirection() == Direction.WEST) {
			if (this.getAvenue() > 1) { plannedAvenue--; } 
			else { plannedStreet++; }
		} else {
			if (this.getAvenue() >= 24) { plannedAvenue--; } 
			else { plannedAvenue++; }
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	/**
	 * Calculates an escape route if a zombie enters the danger radius.
	 * FIXED: Survivors will now slide along walls instead of freezing.
	 */
	private TurnAction evadeZombies(RobotInfoRecord[] state) { 
		if (state.length == 0 || state[0] == null || !state[0].getIsZombie()) {
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

			for (int i = 0; i < availableSpeed; i++) {
				int distAvenue = Math.abs(zombieAvenue - plannedAvenue);
				int distStreet = Math.abs(zombieStreet - plannedStreet);

				// Run away on the axis where the zombie is closest.
				// If they hit a wall, they will automatically slide down the alternate axis!
				if (distAvenue >= distStreet) {
					if (zombieAvenue >= plannedAvenue && plannedAvenue > 1) { plannedAvenue--; } 
					else if (zombieAvenue <= plannedAvenue && plannedAvenue < 24) { plannedAvenue++; } 
					else if (zombieStreet >= plannedStreet && plannedStreet > 1) { plannedStreet--; } 
					else if (zombieStreet <= plannedStreet && plannedStreet < 13) { plannedStreet++; } 
				} else {
					if (zombieStreet >= plannedStreet && plannedStreet > 1) { plannedStreet--; } 
					else if (zombieStreet <= plannedStreet && plannedStreet < 13) { plannedStreet++; } 
					else if (zombieAvenue >= plannedAvenue && plannedAvenue > 1) { plannedAvenue--; } 
					else if (zombieAvenue <= plannedAvenue && plannedAvenue < 24) { plannedAvenue++; } 
				}
			}

			return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
		}
		
		return null;
	}

	/**
	 * Sorts the state array to put the most dangerous zombies at the front
	 */
	private void sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		
		for (int i = 0; i < n - 1; i++) {
			int mostDangerousIndex = i;
			
			for (int j = i + 1; j < n; j++) {
				// FIXED: Added null check to prevent game crash if a robot is missing
				if (state[j] != null && state[j].getIsZombie()) {
					double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());
					double threatScoreJ = distanceJ / Math.max(1, state[j].getSpeed()); // Prevent divide by zero
					
					double currentMinThreatScore = Double.MAX_VALUE;
					if (state[mostDangerousIndex] != null && state[mostDangerousIndex].getIsZombie()) {
						double currentDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
						currentMinThreatScore = currentDistance / Math.max(1, state[mostDangerousIndex].getSpeed());
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
		return "SURVIVOR";
	}
}
