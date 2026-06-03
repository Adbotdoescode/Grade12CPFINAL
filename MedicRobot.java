package final_project_2026;

import java.awt.Color;
import java.util.ArrayList;

import becker.robots.City;
import becker.robots.Direction;

/**
 * This class defines the medic robot whose goal is to heal other robots when there are more zombies than survivors and help gather things when there are more survivors than zombies
 * @author ayyan
 * @version May 25 2026
 */
public class MedicRobot extends GameRobot {

	/**
	 * The constructor for a medic robot 
	 * @param c - city name
	 * @param st - initial street
	 * @param ave - initial avenue
	 * @param dir - initial direction
	 * @param id - robot ID
	 * @param speed - speed of robot
	 * @param isZombie - weather the robot is a zombie or not
	 */
	public MedicRobot(City c, int st, int ave, Direction dir, int id, int speed, boolean isZombie) {
		super(c, st, ave, dir, id, speed, isZombie);
		this.hitPoints = 3;
		this.currentStrategy = "GATHER";
		this.setColor(Color.WHITE);
	}


	private int hitPoints;

	String currentStrategy;

	/**
	 *This method uses the array of records for other robots to help decide weather it is going to heal zombies, gather things or heal itself
	 *@param state - the array of records containing information about each robot that the medic will use to make its decision
	 */

	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) { 
		evaluateStrategy(state);
		TurnAction response = determineResponse(currentStrategy, state);
		return response;
	}

	/**
	 * Depending on the strategy dictated by the evaluateStrategy method, it will calculate the nearest zombie to heal or the nearest thing to collect or move to the storing location to heal itself
	 * @param currentStrategy - The strategy dictating what the main goal of the medic's movement will be
	 * @param state - the list of records providing information on all the other robots
	 * @return - the response that the medic returns to the controller class 
	 */
	private TurnAction determineResponse(String currentStrategy, RobotInfoRecord[] state) {

		ArrayList<ZombieInfoRecord> zombieRecords = new ArrayList<ZombieInfoRecord>(); 

		TurnAction response = new TurnAction(0, 0, "");
		System.out.println(this.currentStrategy);
		if (currentStrategy == "GATHER") {
			System.out.println("Medic is gathering");

		}

		// If the current strategy is heal, then make a separate array containing only the records of zombies and find the closest zombie using selection sort and return an action object requesting to heal it
		else if (currentStrategy == "HEAL") {
//			System.out.println("Medic is healing");

			// For the length of the records array
			for (int i = 0; i < state.length; i++) { 

				// If the robot is a zombie, then add its record to the zombieRecords array
				if (state[i].getIsZombie() == true) { 
					zombieRecords.add(new ZombieInfoRecord(state[i].getId(), state[i].getStreet(), state[i].getAvenue(), state[i].getSpeed(), state[i].getIsZombie(), calculateDistance(this.getStreet(), this.getAvenue() ,state[i].getStreet(), state[i].getAvenue())));
				}
			}

			if (zombieRecords.size() > 0) { 

				// Use selection sort to sort the array of zombie records based on the their distance to the medic (least to greatest)
				// Outer Loop - After loop through and finding the smallest distance record swap it with the index at i and keep repeating this process for the length of zombieRecords
				for (int i = 0; i < zombieRecords.size(); i++) {
					int lastIndex = i;
					double currentMin = zombieRecords.get(i).getDistanceToMedic();

					// Inner loop 
					for (int j = i; j < zombieRecords.size(); j++) { 
						// if the record at j has a closer distance then currentMin, then make it the new minimum and change lastIndex as well
						if (zombieRecords.get(j).getDistanceToMedic() < currentMin) { 
							lastIndex = j;
							currentMin = zombieRecords.get(j).getDistanceToMedic();
						}
					}


					ZombieInfoRecord temp = zombieRecords.get(lastIndex);
					zombieRecords.set(lastIndex, zombieRecords.get(i));
					zombieRecords.set(i, temp);
				}
				//				System.out.println("THE CHOSEN ZOMBIE TO BE HEALED IS: ID NUMBER --> " + zombieRecords.get(0).getId());

				int targetAvenue = zombieRecords.get(0).getAvenue();
				int targetStreet = zombieRecords.get(0).getStreet();
				int streetStepsTotal = zombieRecords.get(0).getStreet() - this.getStreet();
				int avenueStepsTotal = zombieRecords.get(0).getAvenue() - this.getAvenue();
				if (streetStepsTotal < 0) {
					streetStepsTotal *= -1;
				}
				if (avenueStepsTotal < 0) {
					avenueStepsTotal *= -1;
				}
				int totalSteps = streetStepsTotal + avenueStepsTotal;
				int targetBot = zombieRecords.get(0).getId();

				if (totalSteps > this.speed) {

					int difference = totalSteps - speed;
					int avenueSteps = zombieRecords.get(0).getAvenue() - this.getAvenue();
					int streetSteps = zombieRecords.get(0).getStreet() - this.getStreet();
					
					while (difference > 0) {

						if (avenueSteps > 0) {
							avenueSteps--;
							difference--;
						}

						else if (avenueSteps < 0) {
							avenueSteps++;
							difference--;
						}

						else if (streetSteps > 0) {
							streetSteps--;
							difference--;
						}

						else if (streetSteps < 0) {
							streetSteps++;
							difference--;
						}
					}
					
					targetStreet = this.getStreet() + streetSteps;
					targetAvenue = this.getAvenue() + avenueSteps;
				}
				

				response = new TurnAction(targetStreet, targetAvenue, "HEAL");
				response.setTargetBot(targetBot);

			}

			return response;
		}
		return response;
	}


	/**
	 * This method figures out weather the robot will be healing zombies, gathering items or healing itself
	 * @param state - The array of records for each player in the game
	 */
	private void evaluateStrategy(RobotInfoRecord[] state) {
		int survivorCount = 0;
		int zombieCount = 0;

		// Loop over each item in the array of records and for each record, figure out which ones are zombies and which ones are survivors
		for (int i = 0; i < state.length; i++) {
			// If the record is for a zombie, increase zombie count by 1, otherwise survivor count
			if (state[i].getIsZombie() == true) { 
				zombieCount += 1;
			}
			else {
				survivorCount += 1;
			}
		}

		// If the survivor count was greater than zombie count, then healing is not really needed, and in that case, gather, otherwise heal
		if (survivorCount >= zombieCount) { 
			currentStrategy = "HEAL";
		}

		else { 
			currentStrategy = "HEAL";

		}

	}

	@Override
	public RobotInfoRecord generateRecord() {
		return new RobotInfoRecord(this.getId(), this.getStreet(), this.getAvenue(), this.speed, this.isZombie(), 0);
	}

	/**
	 * This method calculates the direct distance from one point to another using pythagorean theorem
	 * @param startingStreet - the street of the starting point
	 * @param startingAvenue - the avenue of the starting point
	 * @param targetStreet - the street of the ending point
	 * @param targetAvenue - the avenue of the ending point
	 */

	protected double calculateDistance(int startingStreet, int startingAvenue, int targetStreet, int targetAvenue) {
		int horizontalDistance = targetAvenue - startingAvenue;
		int verticalDistance = targetStreet - startingStreet;
		double distance = Math.sqrt((horizontalDistance*horizontalDistance) + (verticalDistance*verticalDistance));
		return distance;
	}

	@Override
	public int getCombatAbility() {
		// TODO Auto-generated method stub
		return 0;
	}



}
