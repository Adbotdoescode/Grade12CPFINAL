package final_project_2026;

import java.util.ArrayList;

import becker.robots.*;

public class SurvivorRobot extends GameRobot {

	// these are the variables for the survivors stats and the stuff in their inventory
	private int dodgeAbility;
	private int baseSpeed;
	private int currentItems;
	private int dangerRadius;
	

	/**
	 * constructor setting up the survivor robot
	 * @param c : the city the robot is placed in
	 * @param st : the starting street
	 * @param ave : the starting avenue
	 * @param dir : starting direction
	 * @param id : id of the robot
	 * @param speed : the initial movement speed they start with
	 * @param dodgeAbility : the stat we use to evade zombie attacks
	 */
	public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility) {
		// put false at the end cause its obviously not a zombie
		super(c, st, ave, dir, id, speed, false);
		this.dodgeAbility = dodgeAbility;
		this.baseSpeed = speed;
		this.currentItems = 0; 
		this.dangerRadius = 3;
	}

	/**
	 * this is the state machine that figures out what action to take this turn
	 * @param state : the array of records representing everyone else on the board
	 * @return TurnAction : the chosen action request that gets sent to the app
	 */
	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		// sort the threats first so we know whose closest
		ThreatRecord[] sortedThreats = sortThreats(state);

		// priority 1 is running away if a zombie gets too close to us
		TurnAction evasionAction = evadeZombies(sortedThreats);
		if (evasionAction != null) {
			return evasionAction; 
		}

		// priority 2 is just looking around for more things if we are safe
		return forageMode();
	}

	/**
	 * generates a record of this robot to share with the other players
	 * @return RobotInfoRecord
	 */
	public RobotInfoRecord generateRecord() {
		// their speed actually drops down a bit as they pick up more items
		int dynamicSpeed = Math.max(1, this.baseSpeed - this.currentItems);
		return new RobotInfoRecord(this.id, this.getStreet(), this.getAvenue(), dynamicSpeed, this.isZombie);
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
	 * callback method so the main can tell us we survived an attack
	 * allows the Survivor to learn based on whether or not is dodged an attack and become more/less "confident"
	 */
	public void registerSuccessfulDodge() {
		if (this.dangerRadius > 1) {
			this.dangerRadius++;
			System.out.println("Survivor " + this.id + " learned from the attack. Danger radius increased to " + this.dangerRadius);
		}
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
	 * calculates an escape route if a zombie enters our danger radius
	 * @param state : the sorted array of records
	 * @return TurnAction : the move request, or null if were safe
	 */
	private TurnAction evadeZombies(ThreatRecord[] state) { 
		
		// checks if a zombie is within the danger radius
		boolean isZombieNear = false;
		for (int i = 0; i < state.length; i++) {
			if (state[i].getIsZombie()) {
				double physDist = calculateDistance(state[i].getStreet(), state[i].getAvenue());
				if (physDist <= this.dangerRadius) {
					isZombieNear = true;
					break;
				}

			}
		}
			
		
		if (isZombieNear) {
			
			int availableSpeed = Math.max(1, this.baseSpeed - this.currentItems);
			
			// array to hold all the spots we could potentially run to 
			// size of field is 13 x 24 which is 312 spots in total 
			EscapePoints[] possibleSpots = new EscapePoints[312];
			int spotCount = 0;
			
			// check every single intersection 
			for (int s = 1; s <= 13; s++) {
				for (int a = 1; a <= 24; a++) {
					
					// check if the survivor has enough speed to move here in one turn
					int distanceToSpot = Math.abs(s - this.getStreet()) + Math.abs(a - this.getAvenue());
					
					if (distanceToSpot <= availableSpeed) {
						
						// figure out how safe this spot is 
						// the safety is determined by the distance to the CLOSEST zombie from this spot 
						double minimumZombieDistance = Double.MAX_VALUE;
						
						for (int i = 0; i < state.length; i++) {
							if (state[i].getIsZombie()) {
								// pythagorean theorem from the test spot to the zombie
								double zDist = Math.sqrt(Math.pow(s - state[i].getStreet(), 2) + Math.pow(a - state[i].getAvenue(), 2));
								if (zDist < minimumZombieDistance) {
									minimumZombieDistance = zDist;
								}
							}
						}
						
						// add it to our list of choices
						possibleSpots[spotCount] = new EscapePoints(s, a, minimumZombieDistance);
						spotCount++;
					}
				}
			}
			
			// sort the array using insertion sort (because I used selection sort already for the zombie prioritization)
			// sort from highest safety score to lowest
			for (int i = 1; i < spotCount; i++) {
				EscapePoints key = possibleSpots[i];
				int j = i - 1;
				
				// moving the worse spots down the list
				while (j >= 0 && possibleSpots[j].safetyScore < key.safetyScore) {
					possibleSpots[j + 1] = possibleSpots[j];
					j = j - 1;
				}
				possibleSpots[j + 1] = key;
			}
			
			
			// the MOST safest spot is now at index 0
			EscapePoints bestSpot = possibleSpots[0];
			
			return new TurnAction(bestSpot.street, bestSpot.avenue, TurnAction.MOVE);
		}	
		
		return null;
		
	}

	/**
	 * converts standard records into custom ThreatRecords, then sorts them so we put the most dangerous zombies right at the front
	 * @param state - the array of records
	 * @return ThreatRecord[] - a new, sorted array of custom threat records
	 */
	private ThreatRecord[] sortThreats(RobotInfoRecord[] state) {
		int n = state.length;
		ThreatRecord[] threats = new ThreatRecord[n];

		// convert all records into my custom ThreatRecord
		for (int i = 0; i < n; i ++) {
			double threatScore;

			if (state[i].getIsZombie()) {
				double distance = calculateDistance(state[i].getStreet(), state[i].getAvenue());
				// calculate threat score (distance divdided by speed)
				threatScore = distance  / Math.max(1,  state[i].getSpeed());
			} else {
				// make non-zombies go to the very back by giving them the largest possible value
				threatScore = Double.MAX_VALUE;
			}

			// create the new custom record
			threats[i] = new ThreatRecord(state[i].getId(), state[i].getStreet(), state[i].getAvenue(), state[i].getSpeed(), state[i].getIsZombie(), threatScore); 
		}

		// selection sort algorithm to sort the zombies based on threat score
		for (int i = 0; i < n - 1; i++) {
			int mostDangerousIndex = i;

			for (int j = i + 1; j < n; j++) {
				if (threats[j].getThreatScore() < threats[mostDangerousIndex].getThreatScore()) {
					mostDangerousIndex = j;
				}
			}

			// swapping
			if (mostDangerousIndex != i) {
				ThreatRecord temp = threats[mostDangerousIndex];
				threats[mostDangerousIndex] = threats[i];
				threats[i] = temp;
			}
		}

		return threats;
	}

	
	@Override
	public String getRole() {
		// returning null for the role for right now
		return "SURVIVOR";
	}
	
	// a helper class to store and sort the intersections around the Survivor 
	private class EscapePoints {
		int street; 
		int avenue;
		double safetyScore; 
		
		public EscapePoints(int street, int avenue, double safetyScore) {
			this.street = street;
			this.avenue = avenue;
			this.safetyScore = safetyScore;
		}
	}

	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state, ArrayList<ZombieInfoRecord> zombieRecords) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
