package final_project_2026;

import java.awt.Color;
import java.util.ArrayList;

import becker.robots.*;

/**
 * represents a zombie robot in the game
 * @author Adam
 */
public class ZombieRobot extends GameRobot {

	// variables tracking internal stats and targeting memory
	private int[] estimatedEvadeStats;
	private int currentTargetId;
	private String lastIntent;
	private int attackAbility;

	/**
	 * constructor to create the zombie robot
	 * @param c city robot starts in
	 * @param st int street robot starts in
	 * @param ave int avenue robot starts in
	 * @param dir direction robot faces in initially
	 * @param id int robot id
	 * @param speed int speed of the robot
	 * @param attackAbility int combat strength from 1 to 100
	 * @param totalPlayers int the maximum amount of players in the game
	 */
	public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed, int attackAbility, int totalPlayers) {
		super(c, st, ave, dir, id, speed, true);
		this.setColor(Color.GREEN);
		this.attackAbility = attackAbility;
		this.currentTargetId = -1;
		this.lastIntent = "";

		// variable array scaling with total players to prevent bounds errors
		this.estimatedEvadeStats = new int[totalPlayers]; 

		// loop to initialize memory array with baseline guess
		for(int i = 0; i < totalPlayers; i++) {
			this.estimatedEvadeStats[i] = 25;
		}
	}

	/**
	 * executes turn logic based on given board state
	 * @param state array containing all robot info
	 * @return chosen action parameters
	 */
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		updateLearning(state);

		// variable array isolating valid targets
		SurvivorInfoRecord[] targets = getValidTargets(state);

		// check if array contains viable targets
		if (targets.length > 0) {
			insertionSortTargets(targets);

			// variable holding output intent
			TurnAction action = determineAction(targets);
			this.lastIntent = action.getIntent();
			return action;
		}

		return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
	}

	/**
	 * builds array of valid targets containing enhanced tracking data
	 * @param state array containing all robot info
	 * @return processed array of valid targets
	 */
	private SurvivorInfoRecord[] getValidTargets(RobotInfoRecord[] state) {
		// variables used to map total targets and medic location
		int survivorCount = 0;
		int medicStreet = -1;
		int medicAvenue = -1;

		// loop to locate medic and count valid targets
		for (int i = 0; i < state.length; i++) {
			// check if index contains object
			if (state[i] != null) {
				// check if object represents medic
				if (state[i].getId() == 0) {
					medicStreet = state[i].getStreet();
					medicAvenue = state[i].getAvenue();
				}
				// check if object represents valid survivor
				if (!state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {
					survivorCount++;
				}
			}
		}

		// variable array scoped to counted survivors
		SurvivorInfoRecord[] targets = new SurvivorInfoRecord[survivorCount];

		// variable tracking index for output array
		int index = 0;

		// loop to assemble custom records
		for(int i = 0; i < state.length; i++) {
			// check if valid survivor is located at index
			if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {
				// variable extracting historical evade data
				int knownEvadeStat = this.estimatedEvadeStats[state[i].getId()];

				// variable tracking proximity to medic
				int distToMedic = 999;

				// check if medic coordinates were found on board
				if (medicStreet != -1 && medicAvenue != -1) {
					distToMedic = calculateManhattanDistance(state[i].getStreet(), state[i].getAvenue(), medicStreet, medicAvenue);
				}

				targets[index] = new SurvivorInfoRecord(
						state[i].getId(), 
						state[i].getStreet(), 
						state[i].getAvenue(), 
						state[i].getSpeed(), 
						state[i].getIsZombie(), 
						state[i].getItemsCarried(),
						knownEvadeStat,
						distToMedic
						);
				index++;
			}
		}
		return targets;
	}

	/**
	 * analyzes targets and overrides to format final turn action
	 * @param targets processed array of valid targets
	 * @return generated turn action based on highest priority
	 */
	private TurnAction determineAction(SurvivorInfoRecord[] targets) {
		// check if list contains valid targets
		if(targets.length > 0) {
			// variable tracking priority target
			SurvivorInfoRecord bestTarget = targets[0];

			bestTarget = this.situationalOverride(targets, bestTarget);
			this.currentTargetId = bestTarget.getId();

			// variable tracking steps required to reach target
			int distanceToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			// check if target is reachable in current turn
			if(distanceToBest <= this.getSpeed()) {
				// variable formatting combat interaction request
				TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
				action.setTargetBot(this.currentTargetId);
				return action;
			}
			// fallback moving closer to target
			else {
				// variables tracking current coordinates and remaining movement
				int nextStreet = this.getStreet();
				int nextAvenue = this.getAvenue();
				int speedLeft = (int) this.getSpeed();

				// loop to consume movement while closing distance
				while (speedLeft > 0 && calculateManhattanDistance(nextStreet, nextAvenue, bestTarget.getStreet(), bestTarget.getAvenue()) > 0) {
					// check if vertical displacement exceeds horizontal
					if(Math.abs(bestTarget.getStreet() - nextStreet) > Math.abs(bestTarget.getAvenue() - nextAvenue)) {
						// check if target is located south
						if(bestTarget.getStreet() > nextStreet) { 
							nextStreet++; 
						}
						// fallback if target is located north
						else { 
							nextStreet--; 
						}
					} 
					// fallback if horizontal displacement equals or exceeds vertical
					else {
						// check if target is located east
						if(bestTarget.getAvenue() > nextAvenue) { 
							nextAvenue++; 
						} 
						// fallback if target is located west
						else { 
							nextAvenue--; 
						}
					}
					speedLeft--;
				}
				return new TurnAction(nextStreet, nextAvenue, TurnAction.MOVE);
			}
		}
		return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
	}

	/**
	 * checks specific conditions bypassing mathematical priority logic
	 * @param targets processed array of valid targets
	 * @param bestTarget current priority target
	 * @return overridden priority target
	 */
	private SurvivorInfoRecord situationalOverride(SurvivorInfoRecord[] targets, SurvivorInfoRecord bestTarget) {
		// variable tracking global item state
		int totalItemsOnBoard = 0;

		// loop to sum all items held by survivors
		for(int i = 0; i < targets.length; i++) {
			totalItemsOnBoard+= targets[i].getItemsCarried();
		}
		
		// check if endgame trigger condition is satisfied
		if (totalItemsOnBoard >= 10) {
			// variable tracking reckless target
			SurvivorInfoRecord recklessTarget = targets[0];

			// loop to find target carrying most items
			for (int i = 1; i < targets.length; i++) {
				// check if candidate holds more items than current reckless target
				if (targets[i].getItemsCarried() > recklessTarget.getItemsCarried()) {
					recklessTarget = targets[i];
				} 
				// check if item count is equal
				else if (targets[i].getItemsCarried() == recklessTarget.getItemsCarried()) {
					// variables tracking distance comparison
					int distToCurrent = calculateManhattanDistance(this.getStreet(), this.getAvenue(), targets[i].getStreet(), targets[i].getAvenue());
					int distToReckless = calculateManhattanDistance(this.getStreet(), this.getAvenue(), recklessTarget.getStreet(), recklessTarget.getAvenue());

					// check if candidate is closer
					if (distToCurrent < distToReckless) {
						recklessTarget = targets[i];
					}
				}
			}
			return recklessTarget;
		}

		// loop to check local overrides
		for (int i = 0; i < targets.length; i++) {
			// variables calculating local distances
			SurvivorInfoRecord candidate = targets[i];
			int distToCandidate = calculateManhattanDistance(this.getStreet(), this.getAvenue(), candidate.getStreet(), candidate.getAvenue());
			int distToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			// check if candidate satisfies loot pinata condition
			if (candidate.getItemsCarried() >= 3 && distToCandidate <= 4 && distToBest > 1) {
				bestTarget = candidate;
			}

			// check if candidate satisfies point blank condition
			if (distToCandidate == 1) {
				// check if priority target is further away or yields fewer items
				if (distToBest > 1 || (distToBest == 1 && candidate.getItemsCarried() > bestTarget.getItemsCarried())) {
					bestTarget = candidate;
				}
			}
		}
		//output to prove highest score is chosen (also uncomment the corresponding line in the method insertionSortTargets)
		System.out.println("the best target is: " + bestTarget.getId());
		return bestTarget;
	}

	/**
	 * sorts target array via insertion sort algorithm
	 * @param arr target array to be processed
	 */
	private void insertionSortTargets(SurvivorInfoRecord[] arr) {
		// loop stepping through target elements
		for(int i = 1; i < arr.length; i++) {
			// variables isolating key element and scoring
			SurvivorInfoRecord key = arr[i];
			double keyScore = calculateTargetScore(key);
			int j = i - 1;

			// loop shifting elements with greater score value
			while (j >= 0 && calculateTargetScore(arr[j]) > keyScore) {
				arr[j + 1] = arr[j];
				j = j - 1; 
			}
			arr[j+1] = key;
		}

		// loop to verify mathematical sorting order during testing (also uncomment the corresponding line in the method situationalOverride)
		 for (int k = 0; k < arr.length; k++) { System.out.println("rank " + k + " target id " + arr[k].getId() + " score " + calculateTargetScore(arr[k])); }
	}

	/**
	 * generates mathematical threat score
	 * @param target record being analyzed
	 * @return calculated score determining priority
	 */
	private double calculateTargetScore(SurvivorInfoRecord target) {
		// variables separating raw target metrics
		int distance = calculateManhattanDistance(this.getStreet(), this.getAvenue(), target.getStreet(), target.getAvenue());
		int itemsCarried = target.getItemsCarried();
		int estimatedEvade = target.getEstimatedEvade();
		int distanceToMedic = target.getDistanceToMedic();

		return distance - (itemsCarried * 2.0) + (estimatedEvade / 10.0) - (distanceToMedic * 0.5); 
	}

	/**
	 * processes outcome of previous interactions to map survivor stats
	 * @param state array containing all robot info
	 */
	private void updateLearning(RobotInfoRecord[] state) {
		// check if robot attacked on previous turn
		if(currentTargetId != -1 && lastIntent.equals(TurnAction.INFECT)) {
			// variable tracking target status
			boolean targetStillAlive = false;

			// loop searching array for target
			for(int i = 0; i < state.length; i++) {
				// check if record matches target and retains survivor status
				if(state[i] != null && state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
					targetStillAlive = true;
					break; 
				}
			}

			// check if target evaded interaction
			if(targetStillAlive) {
				estimatedEvadeStats[currentTargetId] += 25;

				// check if tracking stat exceeds bounds
				if (estimatedEvadeStats[currentTargetId] > 100) {
					estimatedEvadeStats[currentTargetId] = 100;
				}
			} 
			// fallback if target was infected
			else {
				currentTargetId = -1;
			}
		}
	}

	/**
	 * calculates grid distance between two coordinate pairs
	 * @param st1 street coordinate primary
	 * @param ave1 avenue coordinate primary
	 * @param st2 street coordinate secondary
	 * @param ave2 avenue coordinate secondary
	 * @return absolute distance sum
	 */
	private int calculateManhattanDistance(int st1, int ave1, int st2, int ave2) {
		return Math.abs(st1 - st2) + Math.abs(ave1 - ave2);
	}

	/**
	 * requests baseline combat stat
	 * @return attack ability integer
	 */
	@Override
	public int getCombatAbility() {
		return this.attackAbility;
	}

	/**
	 * requests entity role classification
	 * @return class role string
	 */
	@Override
	public String getRole() {
		return "ZOMBIE";
	}

	/**
	 * handles partner overloaded request bridging
	 * @param state array containing all robot info
	 * @param zombieRecords secondary list referencing zombie stats
	 * @return mapped output from base implementation
	 */
	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state, ArrayList<ZombieInfoRecord> zombieRecords) {
		return takeTurn(state);
	}
}
