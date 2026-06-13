package final_project_2026;

import java.awt.Color;
import becker.robots.*;

/**
 * zombie robot with goal of finding and infecting survivors
 * @author adam
 */
public class ZombieRobot extends GameRobot {

	private int[] estimatedEvadeStats;
	private int currentTargetId;
	private String lastIntent;
	private int attackAbility;

	/**
	 * constructor to create zombie robot
	 * @param c city robot starts in
	 * @param st int street robot starts in
	 * @param ave int avenue robot starts in
	 * @param dir direction robot faces in initially
	 * @param id int robot id
	 * @param speed int speed of robot
	 * @param attackAbility int combat strength from 1 to 100
	 * @param totalPlayers int maximum amount of players in game
	 */
	public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed, int attackAbility, int totalPlayers) {
		super(c, st, ave, dir, id, speed, true);
		this.setColor(Color.GREEN);
		this.attackAbility = attackAbility;
		this.currentTargetId = -1;
		this.lastIntent = "";

		this.estimatedEvadeStats = new int[totalPlayers]; 

		// loop to set default evade stats
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

		SurvivorInfoRecord[] targets = getValidTargets(state);

		// check if targets exist
		if (targets.length > 0) {
			insertionSortTargets(targets);

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
		int survivorCount = 0;
		int medicStreet = -1;
		int medicAvenue = -1;

		// loop to count survivors and locate medic
		for (int i = 0; i < state.length; i++) {
			// check for valid record
			if (state[i] != null) {
				// check for medic id
				if (state[i].getId() == 0) {
					medicStreet = state[i].getStreet();
					medicAvenue = state[i].getAvenue();
				}
				// check for valid survivor
				if (!state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {
					survivorCount++;
				}
			}
		}

		SurvivorInfoRecord[] targets = new SurvivorInfoRecord[survivorCount];
		int index = 0;

		// loop to populate target array
		for(int i = 0; i < state.length; i++) {
			// check for valid survivor again
			if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {

				int knownEvadeStat = this.estimatedEvadeStats[state[i].getId()];
				int distToMedic = 999;

				// check if medic coordinates are set
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
		// check if targets exist
		if(targets.length > 0) {
			SurvivorInfoRecord bestTarget = targets[0];

			bestTarget = this.situationalOverride(targets, bestTarget);
			this.currentTargetId = bestTarget.getId();

			int distanceToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			// check if target is in range
			if(distanceToBest <= this.getSpeed()) {
				TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
				action.setTargetBot(this.currentTargetId);
				return action;
			}
			else {
				int nextStreet = this.getStreet();
				int nextAvenue = this.getAvenue();
				int speedLeft = (int) this.getSpeed();

				// loop to calculate movement path
				while (speedLeft > 0 && calculateManhattanDistance(nextStreet, nextAvenue, bestTarget.getStreet(), bestTarget.getAvenue()) > 0) {
					// check if street distance is greater than avenue distance
					if(Math.abs(bestTarget.getStreet() - nextStreet) > Math.abs(bestTarget.getAvenue() - nextAvenue)) {
						// check direction for street move
						if(bestTarget.getStreet() > nextStreet) { 
							nextStreet++; 
						}
						else { 
							nextStreet--; 
						}
					} 
					else {
						// check direction for avenue move
						if(bestTarget.getAvenue() > nextAvenue) { 
							nextAvenue++; 
						} 
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
	 * enforces strict phase based priority hierarchy
	 * @param targets processed array of valid targets
	 * @param bestTarget current priority target math winner
	 * @return overridden priority target
	 */
	private SurvivorInfoRecord situationalOverride(SurvivorInfoRecord[] targets, SurvivorInfoRecord bestTarget) {

		int totalItemsOnBoard = 0;
		// loop to sum global items to check for reckless threshold
		for (int i = 0; i < targets.length; i++) {
			totalItemsOnBoard += targets[i].getItemsCarried();
		}

		// priority 1 reckless abandon global items 10 or more
		if (totalItemsOnBoard >= 10) {
			SurvivorInfoRecord recklessTarget = targets[0];
			// loop to find highest item count
			for (int i = 1; i < targets.length; i++) {
				// check if candidate has more items
				if (targets[i].getItemsCarried() > recklessTarget.getItemsCarried()) {
					recklessTarget = targets[i];
				} 
				// check for closer distance on tie
				else if (targets[i].getItemsCarried() == recklessTarget.getItemsCarried()) {
					int distToCurrent = calculateManhattanDistance(this.getStreet(), this.getAvenue(), targets[i].getStreet(), targets[i].getAvenue());
					int distToReckless = calculateManhattanDistance(this.getStreet(), this.getAvenue(), recklessTarget.getStreet(), recklessTarget.getAvenue());

					// check distance comparison
					if (distToCurrent < distToReckless) {
						recklessTarget = targets[i];
					}
				}
			}
			return recklessTarget;
		}

		// priority 2 point blank override distance 1
		SurvivorInfoRecord pointBlankTarget = null;
		// loop to check point blank conditions
		for (int i = 0; i < targets.length; i++) {
			int distToCandidate = calculateManhattanDistance(this.getStreet(), this.getAvenue(), targets[i].getStreet(), targets[i].getAvenue());

			// check exact adjacent distance
			if (distToCandidate == 1) {
				// prioritize highest item count if multiple targets are adjacent
				if (pointBlankTarget == null || targets[i].getItemsCarried() > pointBlankTarget.getItemsCarried()) {
					pointBlankTarget = targets[i];
				}
			}
		}

		// check if point blank target was found
		if (pointBlankTarget != null) {
			return pointBlankTarget;
		}

		// priority 3 greedy override items 3 or more and distance 8 or less
		SurvivorInfoRecord greedyTarget = null;
		// loop to check greedy conditions
		for (int i = 0; i < targets.length; i++) {
			int distToCandidate = calculateManhattanDistance(this.getStreet(), this.getAvenue(), targets[i].getStreet(), targets[i].getAvenue());

			// check item and distance thresholds
			if (targets[i].getItemsCarried() >= 3 && distToCandidate <= 8) {
				// prioritize highest item count break ties by closest distance
				if (greedyTarget == null || targets[i].getItemsCarried() > greedyTarget.getItemsCarried()) {
					greedyTarget = targets[i];
				} 
				// check distance tie breaker
				else if (targets[i].getItemsCarried() == greedyTarget.getItemsCarried()) {
					int distToGreedy = calculateManhattanDistance(this.getStreet(), this.getAvenue(), greedyTarget.getStreet(), greedyTarget.getAvenue());

					// check distance comparison
					if (distToCandidate < distToGreedy) {
						greedyTarget = targets[i];
					}
				}
			}
		}

		// check if greedy target was found
		if (greedyTarget != null) {
			return greedyTarget;
		}

		// priority 4 mathematical winner
		return bestTarget;
	}

	/**
	 * sorts target array via insertion sort algorithm
	 * @param arr target array to be processed
	 */
	private void insertionSortTargets(SurvivorInfoRecord[] arr) {
		// loop to sort array
		for(int i = 1; i < arr.length; i++) {
			SurvivorInfoRecord key = arr[i];
			double keyScore = calculateTargetScore(key);
			int j = i - 1;

			// loop to shift elements
			while (j >= 0 && calculateTargetScore(arr[j]) > keyScore) {
				arr[j + 1] = arr[j];
				j = j - 1; 
			}
			arr[j+1] = key;
		}
	}

	/**
	 * generates mathematical threat score
	 * @param target record being analyzed
	 * @return calculated score determining priority
	 */
	private double calculateTargetScore(SurvivorInfoRecord target) {
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
		// check if valid attack occurred
		if(currentTargetId != -1 && lastIntent.equals(TurnAction.INFECT)) {
			boolean targetStillAlive = false;

			// loop to find target
			for(int i = 0; i < state.length; i++) {
				// check if target matches and is alive
				if(state[i] != null && state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
					targetStillAlive = true;
					break; 
				}
			}

			// check alive status
			if(targetStillAlive) {
				estimatedEvadeStats[currentTargetId] += 25;

				// check cap limit
				if (estimatedEvadeStats[currentTargetId] > 100) {
					estimatedEvadeStats[currentTargetId] = 100;
				}
			} 
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

}
