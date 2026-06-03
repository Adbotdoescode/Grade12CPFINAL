package final_project_2026;

import becker.robots.*;

public class SurvivorRobot extends GameRobot {

	private int dodgeAbility;
	private int baseSpeed;
	private int maxCapacity;
	private int currentItems;

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

		TurnAction evasionAction = evadeZombies(state);
		if (evasionAction != null) {
			return evasionAction; 
		}
		
		if (this.currentItems >= this.maxCapacity) {
			return deliveryMode();
		}

		return forageMode();
	}

	// NEW: Required by Adam's updated GameRobot class
	@Override
	public RobotInfoRecord generateRecord() {
		// Survivor's speed decreases as items are picked up
		int dynamicSpeed = Math.max(1, this.baseSpeed - this.currentItems);
		return new RobotInfoRecord(this.id, this.getStreet(), this.getAvenue(), dynamicSpeed, this.isZombie);
	}

	// NEW: Required by Adam's updated GameRobot class
	@Override
	public int getCombatAbility() {
		// The survivor's "combat ability" is its ability to dodge
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
			plannedAvenue++; 
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	private TurnAction deliveryMode() {
		if (this.getStreet() == 1 && this.getAvenue() == 1) {
			this.currentItems = 0; 
			return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.DROP_OFF);
		}

		int plannedStreet = this.getStreet();
		int plannedAvenue = this.getAvenue();

		int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);

		for (int i = 0; i < availableSpeed; i++) {
			if (plannedAvenue > 1) { plannedAvenue--; }
			else if (plannedStreet > 1) { plannedStreet--; }
		}

		return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
	}

	private TurnAction evadeZombies(RobotInfoRecord[] state) { 
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

			for (int i = 0; i < availableSpeed; i++) {
				if (zombieAvenue >= plannedAvenue && plannedAvenue > 1) {
					plannedAvenue--;
				} 
				else if (zombieAvenue <= plannedAvenue && plannedAvenue < 24) {
					plannedAvenue++;
				}
				else if (zombieStreet <= plannedStreet && plannedStreet < 13) {
					plannedStreet++;
				}
				else if (zombieStreet >= plannedStreet && plannedStreet > 1) {
					plannedStreet--;
				}
			}

			return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
		}
		return null;
	}

	private void sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		for (int i = 0; i < n - 1; i++) {
			int mostDangerousIndex = i;
			for (int j = i + 1; j < n; j++) {
				if (state[j].getIsZombie()) {
					double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());
					double threatScoreJ = distanceJ / state[j].getSpeed();
					
					double currentMinThreatScore;
					if (state[mostDangerousIndex].getIsZombie()) {
						double currentDistance = calculateDistance(state[mostDangerousIndex].getStreet(), state[mostDangerousIndex].getAvenue());
						currentMinThreatScore = currentDistance / state[mostDangerousIndex].getSpeed();
					} else {
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
}
