package final_project_2026;

import java.awt.Color;
import becker.robots.*;

/**
 * Zombie robot with the goal of finding and infecting survivors
 * @author Adam
 */
public class ZombieRobot extends GameRobot {

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

		this.estimatedEvadeStats = new int[totalPlayers]; 

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

		for (int i = 0; i < state.length; i++) {
			if (state[i] != null) {
				if (state[i].getId() == 0) {
					medicStreet = state[i].getStreet();
					medicAvenue = state[i].getAvenue();
				}
				if (!state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {
					survivorCount++;
				}
			}
		}

		SurvivorInfoRecord[] targets = new SurvivorInfoRecord[survivorCount];
		int index = 0;

		for(int i = 0; i < state.length; i++) {
			if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id && state[i].getId() != 0) {
				
				int knownEvadeStat = this.estimatedEvadeStats[state[i].getId()];
				int distToMedic = 999;

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
		if(targets.length > 0) {
			SurvivorInfoRecord bestTarget = targets[0];

			bestTarget = this.situationalOverride(targets, bestTarget);
			this.currentTargetId = bestTarget.getId();

			int distanceToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			if(distanceToBest <= this.getSpeed()) {
				TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
				action.setTargetBot(this.currentTargetId);
				return action;
			}
			else {
				int nextStreet = this.getStreet();
				int nextAvenue = this.getAvenue();
				int speedLeft = (int) this.getSpeed();

				while (speedLeft > 0 && calculateManhattanDistance(nextStreet, nextAvenue, bestTarget.getStreet(), bestTarget.getAvenue()) > 0) {
					if(Math.abs(bestTarget.getStreet() - nextStreet) > Math.abs(bestTarget.getAvenue() - nextAvenue)) {
						if(bestTarget.getStreet() > nextStreet) { 
							nextStreet++; 
						}
						else { 
							nextStreet--; 
						}
					} 
					else {
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
	 * @param targets processed array of valid targets
	 * @param bestTarget current priority target
	 * @return overridden priority target
	 */
	private SurvivorInfoRecord situationalOverride(SurvivorInfoRecord[] targets, SurvivorInfoRecord bestTarget) {
		int totalItemsOnBoard = 0;

		for(int i = 0; i < targets.length; i++) {
			totalItemsOnBoard+= targets[i].getItemsCarried();
		}
		
		if (totalItemsOnBoard >= 10) {
			SurvivorInfoRecord recklessTarget = targets[0];

			for (int i = 1; i < targets.length; i++) {
				if (targets[i].getItemsCarried() > recklessTarget.getItemsCarried()) {
					recklessTarget = targets[i];
				} 
				else if (targets[i].getItemsCarried() == recklessTarget.getItemsCarried()) {
					int distToCurrent = calculateManhattanDistance(this.getStreet(), this.getAvenue(), targets[i].getStreet(), targets[i].getAvenue());
					int distToReckless = calculateManhattanDistance(this.getStreet(), this.getAvenue(), recklessTarget.getStreet(), recklessTarget.getAvenue());

					if (distToCurrent < distToReckless) {
						recklessTarget = targets[i];
					}
				}
			}
			return recklessTarget;
		}

		for (int i = 0; i < targets.length; i++) {
			SurvivorInfoRecord candidate = targets[i];
			int distToCandidate = calculateManhattanDistance(this.getStreet(), this.getAvenue(), candidate.getStreet(), candidate.getAvenue());
			int distToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			if (candidate.getItemsCarried() >= 3 && distToCandidate <= 4 && distToBest > 1) {
				bestTarget = candidate;
			}

			if (distToCandidate == 1) {
				if (distToBest > 1 || (distToBest == 1 && candidate.getItemsCarried() > bestTarget.getItemsCarried())) {
					bestTarget = candidate;
				}
			}
		}
		
		//output to prove highest score is chosen (also uncomment the corresponding line in the method insertionSortTargets)
		//System.out.println("the best target is: " + bestTarget.getId());
		return bestTarget;
	}

	/**
	 * sorts target array via insertion sort algorithm
	 * @param arr target array to be processed
	 */
	private void insertionSortTargets(SurvivorInfoRecord[] arr) {
		for(int i = 1; i < arr.length; i++) {
			SurvivorInfoRecord key = arr[i];
			double keyScore = calculateTargetScore(key);
			int j = i - 1;

			while (j >= 0 && calculateTargetScore(arr[j]) > keyScore) {
				arr[j + 1] = arr[j];
				j = j - 1; 
			}
			arr[j+1] = key;
		}

		// loop to verify mathematical sorting order during testing (also uncomment the corresponding line in the method situationalOverride)
		// for (int k = 0; k < arr.length; k++) { System.out.println("rank " + k + " target id " + arr[k].getId() + " score " + calculateTargetScore(arr[k])); }
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
		if(currentTargetId != -1 && lastIntent.equals(TurnAction.INFECT)) {
			boolean targetStillAlive = false;

			for(int i = 0; i < state.length; i++) {
				if(state[i] != null && state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
					targetStillAlive = true;
					break; 
				}
			}

			if(targetStillAlive) {
				estimatedEvadeStats[currentTargetId] += 25;

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
