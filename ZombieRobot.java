package final_project_2026;

import java.awt.Color;
import becker.robots.*;

/**
 * represents a zombie robot that hunts down survivors based on distance,
 * items carried, and past evasion success.
 * @author Adam
 * @version May 26, 2026
 */
public class ZombieRobot extends GameRobot {

	// instance variables to store learning info
	private int[] learnedEvasionStats;
	private int currentTargetId;

	/**
	 * creates a new zombie robot
	 * @param c the city the robot is in
	 * @param st the starting street
	 * @param ave the starting avenue
	 * @param dir the starting direction
	 * @param id the unique identifier for this robot
	 * @param speed the movement speed of this robot
	 */
	public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed) {
		super(c, st, ave, dir, id, speed, true);
		this.setColor(Color.RED);
		this.learnedEvasionStats = new int[50]; 
		this.currentTargetId = -1;
	}

	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) {
		// update learning based on last turn
		updateLearning(state);

		// get all valid survivor targets
		RobotInfoRecord[] targets = getValidTargets(state);

		// sort targets by threat score
		insertionSortTargets(targets);

		// determine move or attack action
		return determineAction(targets);
	}

	/**
	 * filters the state array to find only living survivors
	 * @param state the current board state
	 * @return an array containing only valid targets
	 */
	private RobotInfoRecord[] getValidTargets(RobotInfoRecord[] state) {
		// count valid survivors to size the array
		int survivorCount = 0;
		for (int i = 0; i < state.length; i++) {
			if (!state[i].getIsZombie() && state[i].getId() != this.id) {
				survivorCount++;
			}
		}

		// create array and populate it
		RobotInfoRecord[] targets = new RobotInfoRecord[survivorCount];
		int index = 0;
		for(int i = 0; i < state.length; i++) {
			if(!state[i].getIsZombie() && state[i].getId() != this.id) {
				targets[index] = state[i];
				index++;
			}
		}
		
		return targets;
	}

	/**
	 * decides whether to infect or move towards the best target
	 * @param targets the sorted array of valid targets
	 * @return the action for this turn
	 */
	private TurnAction determineAction(RobotInfoRecord[] targets) {
		// check if there are targets left
		if(targets.length > 0) {
			// best target is now at index 0
			RobotInfoRecord bestTarget = targets[0];
			
			// remember for next turn
			this.currentTargetId = bestTarget.getId();

			double distanceToBest = calculateDistance(bestTarget.getStreet(), bestTarget.getAvenue());

			// if the distance is next to the zombie
			if(distanceToBest <= 1.0) {
				return new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), "INFECT");
			}
			// otherwise move to them
			else {
				int nextStreet = this.getStreet();
				int nextAvenue = this.getAvenue();

				// closing gap on the longest axis
				if(Math.abs(bestTarget.getStreet() - this.getStreet()) > Math.abs(bestTarget.getAvenue() - this.getAvenue())) {
					if(bestTarget.getStreet() > this.getStreet()) {
						nextStreet++;
					} else { 
						nextStreet--;
					}
				} else {
					if(bestTarget.getAvenue() > this.getAvenue()) {
						nextAvenue++;
					} else {
						nextAvenue--;
					}
				}
				
				return new TurnAction(nextStreet, nextAvenue, "MOVE");
			}
		}

		// fall back if no targets exist
		return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
	}

	/**
	 * sorts the array of targets based on the 3 criteria
	 * lower score = better target
	 * @param arr the array of robots to sort
	 */
	private void insertionSortTargets(RobotInfoRecord[] arr) {
		// start at 1 for insertion sort
		for(int i = 1; i < arr.length; i++) {
			RobotInfoRecord key = arr[i];
			double keyScore = calculateTargetScore(key);
			int j = i - 1;

			// shift elements that have a higher score than the key
			while (j >= 0 && calculateTargetScore(arr[j]) > keyScore) {
				arr[j + 1] = arr[j];
				j = j - 1; 
			}
			arr[j+1] = key;
		}
	}

	/**
	 * calculates the threat score using the 3 minimum criteria
	 * @param target the robot to evaluate
	 * @return the calculated threat score
	 */
	private double calculateTargetScore(RobotInfoRecord target) {
		double distance = calculateDistance(target.getStreet(), target.getAvenue());
		int itemsCarried = target.getItemsCarried();
		int evasions = learnedEvasionStats[target.getId()];

		// distance is bad, so adds to score
		// items are good, so they subtract from score
		// high evasion are bad, add heavily to score
		return distance - (itemsCarried * 2.0) + (evasions * 3.0); 
	}

	/**
	 * evaluates what happened since the last turn to update the learning about survivors
	 * @param state the current board state
	 */
	private void updateLearning(RobotInfoRecord[] state) {
		// check if we had a target last turn
		if(currentTargetId != -1) {
			boolean targetStillAlive = false;

			// search the state array for our target
			for(int i = 0; i < state.length; i++) {
				if(state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
					targetStillAlive = true;
					break; // exit loop once found
				}
			}

			// if alive, means chased but failed to infect
			// increment evasion stat
			if(targetStillAlive) {
				learnedEvasionStats[currentTargetId]++;
			} else {
				// reset target if infected
				currentTargetId = -1;
			}
		}
	}
}