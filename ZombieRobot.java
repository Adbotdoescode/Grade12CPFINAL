package final_project_2026;

import java.awt.Color;
import java.util.ArrayList;

import becker.robots.*;

/**
 * represents a zombie robot in the game
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
	 * @param totalPlayers int the maximum amount of players in the game (for memory sizing)
	 */
	public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed, int attackAbility, int totalPlayers) {
		super(c, st, ave, dir, id, speed, true);
		this.setColor(Color.GREEN);
		this.attackAbility = attackAbility;
		this.currentTargetId = -1;
		this.lastIntent = "";

		// array scales perfectly with the game size 
		this.estimatedEvadeStats = new int[totalPlayers]; 

		// initialize all estimates to a baseline quadrant 1 guess
		for(int i = 0; i < totalPlayers; i++) {
			this.estimatedEvadeStats[i] = 25;
		}
	}

	/**
	 * executes the zombie's turn logic
	 * @param state array containing all robot info
	 * @return the action the zombie will take
	 */
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		updateLearning(state);

		SurvivorInfoRecord[] targets = getValidTargets(state);
		insertionSortTargets(targets);

		TurnAction action = determineAction(targets);

		this.lastIntent = action.getIntent();
		return action;
	}

	/**
	 * filters out invalid targets from the game state
	 * @param state array containing all robot info
	 * @return array of valid survivor targets
	 */
	private SurvivorInfoRecord[] getValidTargets(RobotInfoRecord[] state) {
		int survivorCount = 0;
		for (int i = 0; i < state.length; i++) {
			if (state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
				survivorCount++;
			}
		}

		SurvivorInfoRecord[] targets = new SurvivorInfoRecord[survivorCount];
		int index = 0;

		for(int i = 0; i < state.length; i++) {
			if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
				int knownEvadeStat = this.estimatedEvadeStats[state[i].getId()];

				targets[index] = new SurvivorInfoRecord(
						state[i].getId(), 
						state[i].getStreet(), 
						state[i].getAvenue(), 
						state[i].getSpeed(), 
						state[i].getIsZombie(), 
						state[i].getItemsCarried(),
						knownEvadeStat
						);
				index++;
			}
		}
		return targets;
	}

	/**
	 * determines the best action based on available targets
	 * @param targets array of valid survivor targets
	 * @return the chosen turn action
	 */
	private TurnAction determineAction(SurvivorInfoRecord[] targets) {
		if(targets.length > 0) {
			// start with the mathematically best target from the insertion sort
			SurvivorInfoRecord bestTarget = targets[0];

			bestTarget = this.situationalOverride(targets, bestTarget);

			this.currentTargetId = bestTarget.getId();
			int distanceToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			// if the target is within the zombie's speed limit
			if(distanceToBest <= this.getSpeed()) {
				TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
				action.setTargetBot(this.currentTargetId);
				return action;
			}
			// if target is out of range, sprint towards them using all available speed
			else {
				int nextStreet = this.getStreet();
				int nextAvenue = this.getAvenue();
				int speedLeft = (int) this.getSpeed();

				// loop to consume all movement points to get as close as possible
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
	 * checks for special conditions that should override normal targeting
	 * @param targets array of valid survivor targets
	 * @param bestTarget current best target
	 * @return potentially overridden target
	 */
	private SurvivorInfoRecord situationalOverride(SurvivorInfoRecord[] targets, SurvivorInfoRecord bestTarget) {
		// scan the available targets to see if a situational override is needed
		for (int i = 0; i < targets.length; i++) {
			SurvivorInfoRecord candidate = targets[i];
			int distToCandidate = calculateManhattanDistance(this.getStreet(), this.getAvenue(), candidate.getStreet(), candidate.getAvenue());
			int distToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

			// if a survivor has 3 or more items and is somewhat close (within 4 tiles), prioritize
			// do not override if the current best target is 1 step away
			if (candidate.getItemsCarried() >= 3 && distToCandidate <= 4 && distToBest > 1) {
				bestTarget = candidate;
			}

			// iff a survivor is 1 block away, ignore their high evade/empty backpack and prioritize them immediately
			if (distToCandidate == 1) {
				// only override if the current best isn't ALSO 1 block away with more items
				if (distToBest > 1 || (distToBest == 1 && candidate.getItemsCarried() > bestTarget.getItemsCarried())) {
					bestTarget = candidate;
				}
			}
		}
		return bestTarget;
	}

	/**
	 * sorts targets by their calculated score
	 * @param arr array of targets to sort
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
	}

	/**
	 * calculates a score for target prioritization
	 * @param target the survivor to score
	 * @return calculated score value
	 */
	private double calculateTargetScore(SurvivorInfoRecord target) {
		int distance = calculateManhattanDistance(this.getStreet(), this.getAvenue(), target.getStreet(), target.getAvenue());
		int itemsCarried = target.getItemsCarried();
		int estimatedEvade = target.getEstimatedEvade();

		return distance - (itemsCarried * 2.0) + (estimatedEvade / 10.0); 
	}

	/**
	 * updates the zombie's evasion estimates based on previous actions
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
	 * calculates manhattan distance between two points
	 * @param st1 first street coordinate
	 * @param ave1 first avenue coordinate
	 * @param st2 second street coordinate
	 * @param ave2 second avenue coordinate
	 * @return the calculated distance
	 */
	private int calculateManhattanDistance(int st1, int ave1, int st2, int ave2) {
		return Math.abs(st1 - st2) + Math.abs(ave1 - ave2);
	}

	@Override
	public int getCombatAbility() {
		return this.attackAbility;
	}

	@Override
	public String getRole() {
		return "ZOMBIE";
	}


}
