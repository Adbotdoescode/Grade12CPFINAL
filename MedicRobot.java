package final_project_2026;

import java.awt.Color;
import java.util.Random;

import becker.robots.City;
import becker.robots.Direction;

/**
 * This class defines the medic robot which heals other zombies through sorting by speed, distance and dodgeAbility (a learned attribute)
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
	public MedicRobot(City c, int st, int ave, Direction dir, int id, int speed, boolean isZombie, int playerCount) {
		super(c, st, ave, dir, id, speed, isZombie);
		this.setColor(Color.WHITE);
		// this.setLabel(" ID " + this.id); 
		zombieRecords = new ZombieInfoRecord[playerCount]; 
	}


	// Instance variables are initialized 
	private int lastTargetStreet;
	private int lastTargetAvenue;
	private int lastTargetZombie;
	double dodgeAbility = 0;
	int totalDodges = 0; 
	int totalAttacks = 0;
	private ZombieInfoRecord[] zombieRecords;

	Random generator = new Random();
	private int ability = generator.nextInt(100)+1;




	/**
	 *This method uses the array of records for other robots to decide the best possible move it can make 
	 *@param state - the array of records containing information about each robot that the medic will use to make its decision
	 */

	@Override
	public TurnAction takeTurn(RobotInfoRecord[] state) { 
		TurnAction response = determineResponse(state);
		return response;
	}

	/**
	 * The medic will calculate the best zombie to attempt to heal based on speed, distance and dodgeAbility 
	 * @param state - the list of records providing information on all the other robots
	 * @return - the response that the medic returns to the controller class in the form of a record called TurnAction
	 */
	private TurnAction determineResponse(RobotInfoRecord[] state) {

		// initialize variables for this method
		double maxSpeed = 10;
		double maxDistance = Math.sqrt((10*10)+(10*10));
		TurnAction response = new TurnAction(0, 0, ""); 
		int targetAvenue = 0;
		int targetStreet = 0;
		int targetBot = 0;
		int totalSteps = 0;

		// For the length of the records array
		for (int i = 0; i < state.length; i++) { 


			// we need to save the values of dodgeAbility, totalDodges and totalAttacks before we begin wiping out and replacing zombieInfoRecords. However, do not do this if zombieRecords[i] is equal to null to avoid a nullPointerException
			if (zombieRecords[i] != null) {
				dodgeAbility = zombieRecords[i].getDodgeAbility();
				totalDodges = zombieRecords[i].getTotalDodges();
				totalAttacks = zombieRecords[i].getTotalAttacks();
			}


			// begin replacing each item in zombieInfoRecord with the updated information from the robotInfoRecord array. 
			// Since the order of the items in the array DO NOT match up in terms of ID, an inner loop is needed to search for the index with the correct ID inside of the state array (of type robotInfoRecord)
			for (int j = 0; j < state.length; j++) { 
				if (zombieRecords[i] != null && state[j].getId() == zombieRecords[i].getId()) {
					zombieRecords[i] = (new ZombieInfoRecord(state[j].getId(), state[j].getStreet(), state[j].getAvenue(), state[j].getSpeed(), state[j].getIsZombie(), calculateDistance(this.getStreet(), this.getAvenue() ,state[j].getStreet(), state[j].getAvenue())));
					zombieRecords[i].setDodgeAbility(dodgeAbility);
					zombieRecords[i].setTotalAttacks(totalAttacks);
					zombieRecords[i].setTotalDodges(totalDodges);
				}
			} 

			// However, if the element at zombieRecord's index is null, then that means this is the medic's very first move. In that case, directly copy every value from state into each corresponding element in zombieRecord, no searching is needed here
			if (zombieRecords[i] == null) { 
				zombieRecords[i] = (new ZombieInfoRecord(state[i].getId(), state[i].getStreet(), state[i].getAvenue(), state[i].getSpeed(), state[i].getIsZombie(), calculateDistance(this.getStreet(), this.getAvenue() ,state[i].getStreet(), state[i].getAvenue())));
				zombieRecords[i].setDodgeAbility(dodgeAbility);
				zombieRecords[i].setTotalAttacks(totalAttacks);
				zombieRecords[i].setTotalDodges(totalDodges);
			}
		}


		// Loop over each element inside of zombieRecords
		for (int i = 0; i < zombieRecords.length; i++) { 

			// if the record at index i has the same id as the lastTargetZombie that the medic attempted to heal and if the medic is on the same spot as where that zombie was a move ago, then this means the medic attempted to heal and we can use this to learn about the zombie's dodgeAbility
			if (zombieRecords[i].getId() != this.getId() && zombieRecords[i].getId() == lastTargetZombie && this.getStreet() == lastTargetStreet && this.getAvenue() == lastTargetAvenue) {

					// if that zombie is still a zombie, that means it successfully dodged the medic. In that case, we will increase both dodges and total attacks and determine the new value for dodgeAbility as a fraction of the totalDodges / totalAttacks
					if (zombieRecords[i].getIsZombie() == true) {
						zombieRecords[i].increaseDodges();
						zombieRecords[i].increaseTotalAttacks(); 
						zombieRecords[i].setDodgeAbility((double) zombieRecords[i].getTotalDodges() / (double) zombieRecords[i].getTotalAttacks()); 

					}
					else  {
						zombieRecords[i].increaseTotalAttacks();
						zombieRecords[i].setDodgeAbility((double) zombieRecords[i].getTotalDodges() / (double) zombieRecords[i].getTotalAttacks());
					}

			}
		} 

		// Loop over the length of zombieRecords and determine totalUndesirability by passing the distance, speed, and dodgeAbility into the calculatePreferability method
		for (int i = 0; i < zombieRecords.length; i++) { 
			zombieRecords[i].setTotalUndesirability(calculateUndesirability(calculateDistance(this.getStreet(), this.getAvenue() ,zombieRecords[i].getStreet(), zombieRecords[i].getAvenue()), zombieRecords[i].getSpeed(), maxDistance, maxSpeed, zombieRecords[i].getDodgeAbility(), zombieRecords[i].getTotalAttacks()));
		} 

		if (zombieRecords.length > 0) { 

			// Use selection sort to sort the array of zombie records based on the their distance to the medic (least to greatest)
			// Outer Loop - After loop through and finding the smallest totalUndeserability record swap it with the index at i and keep repeating this process for the length of zombieRecords
			for (int i = 0; i < zombieRecords.length; i++) {
				int lastIndex = i;
				double currentMax = zombieRecords[i].getTotalUndesirability();

				// Inner loop 
				for (int j = i; j < zombieRecords.length; j++) { 
					// if the record at j has a smaller totalUndeserability then currentMin, then make it the new minimum and change lastIndex as well
					if (zombieRecords[j].getTotalUndesirability() < currentMax) { 
						lastIndex = j;
						currentMax = zombieRecords[j].getTotalUndesirability();
					}
				}

				// swapping the record at index i with the record at lastIndex
				ZombieInfoRecord temp = zombieRecords[lastIndex];
				zombieRecords[lastIndex] = zombieRecords[i];
				zombieRecords[i] = temp;
			}

// 			DEBUG STATEMENT TO CHECK SELECTION SORT
//			for (int i = 0; i < zombieRecords.length; i++ ) { 
//				System.out.println("Zombie ID: " + zombieRecords[i].getId() + " TOTAL UNDESIRABILITY: " + zombieRecords[i].getTotalUndesirability() + " ISZOMBIE: " + zombieRecords[i].getIsZombie());
//			}

   
			// Loop over the entire length of zombieRecords. Since zombieRecords contains both survivors and zombies sorted, this loop will find the first ZOMBIE with the smallest undesirablity number
			for (int j = 0; j < zombieRecords.length; j++) { 
				// if the record at index j is a zombie and is not the medic itself, then that is the zombie with the least undesirability. This zombie is then used to set the target avenue, target street and target bot
				if (zombieRecords[j].getIsZombie() == true && zombieRecords[j].getId() != this.getId()) { 
					targetAvenue = zombieRecords[j].getAvenue();
					targetStreet = zombieRecords[j].getStreet();
					targetBot = zombieRecords[j].getId();
					break;
				}  
			}
 
			// Determine the total street steps and avenue steps to determine the total # of steps
			int avenueSteps = targetAvenue - this.getAvenue(); 
			int streetSteps = targetStreet - this.getStreet();
			totalSteps = Math.abs(avenueSteps) + Math.abs(streetSteps);
			int difference = totalSteps - speed;
			
			// If total steps is greater than speed, then we must reduce our targetStreet and targetAvenue to prevent an illegal move
			if (totalSteps > this.speed) {

				// The difference is the difference between the current number of steps required and the speed limit. It indicates how many steps must be taken off in order to make the move legal. Once difference reaches zero, the move becomes legal
				while (difference > 0) {

					// Start by decreasing the avenue steps by one (if avenue steps is positive). Decrease difference at the same time
					if (avenueSteps > 0) {
						avenueSteps--;
						difference--;
//						System.out.println("NEW DIFFERENCE " + difference); 
//						System.out.println("NEW avenueSteps" + avenueSteps); 
					}

					// If avenueSteps is negative, then increase avenueSteps to reduce the total number of steps
					else if (avenueSteps < 0) {
						avenueSteps++;
						difference--;
						
					}

					// Once avenueSteps is equal to 0, neither of the first two statements are able to run. In that case, begin shortening the streetSteps. Decreasing streetSteps if it is positive 
					else if (streetSteps > 0) {
						streetSteps--;
						difference--;
					}

					// If streetSteps is negative, then increase avenueSteps to reduce the total number of steps
					else if (streetSteps < 0) {
						streetSteps++;
						difference--;
					}
				}

				// calculate the new targetStreet and targetAvenue
				targetStreet = this.getStreet() + streetSteps;
				targetAvenue = this.getAvenue() + avenueSteps;
				
			}

			response = new TurnAction(targetStreet, targetAvenue, "HEAL");
			response.setTargetBot(targetBot);

			// update lastTargetStreet, lastTargetAvenue and lastTargetZombie to help determine dodgeAbility the next time this method is ran
			lastTargetStreet = response.getTargetStreet();
			lastTargetAvenue = response.getTargetAvenue();
			lastTargetZombie = response.getTargetBot();
			
		}

		return response;
	}


	/**
	 * This method calculates the total undesirability which combines the speed and distanceToMedic attributes of the zombie. Uses equivalent fractions to weigh both speed and distance equally 
	 * First factors are determines for distance, speed and dodgeAbility (the number that must be multiplied to bring the max values for each attribute to 100). These factors are then applied to each of the attributes we are sorting by 
	 * @param distance - the distance of the zombie to the medic
	 * @param speed - the speed of the zombie
	 * @param maxDistance - the maxDistance a zombie can be from a the medic
	 * @param maxSpeed - The max speed a zombie can have
	 * @return - returns the total calculated undesirability being the sum of the speed over the maxSpeed and distance over the maxDistance
	 */
	private double calculateUndesirability(double distance, double speed, double maxDistance, double maxSpeed, double dodgeAbility, double totalAttacks) {
		
		double distanceFactor = 100 / maxDistance;
		double speedFactor = 100 / maxSpeed;
		double dodgeFactor = 100 / 1;
		
		
		maxDistance *= distanceFactor;
		maxSpeed *= speedFactor;    
		distance *= distanceFactor;
		distance *= 1.25;
		speed *= speedFactor;

		// Since one attack is not enough to accurately estimate each robots dodgeAbility, dodgeAbility is only applied after 3 attacks
		// Note, totalAttacks is set to be greater than 2, however dodgeAbility will be applied after 3 complete attacks not 2 because during the 4th attack is when totalAttacks will be 3 (since totalAttacks is always calculated on the NEXT turn)
		if (totalAttacks > 2) {
			dodgeAbility *= dodgeFactor;
		}
		else { 
			dodgeAbility = 0;
		}

		return distance + speed + dodgeAbility;
	}


	/**
	 * This method calculates the direct distance from one point to another using pythagorean theorem. It takes the (square root) of the difference between the avenues (squared) + difference between the streets (squared)
	 * @param startingStreet - the street of the starting point
	 * @param startingAvenue - the avenue of the starting point 
	 * @param targetStreet - the street of the ending point
	 * @param targetAvenue - the avenue of the ending point
	 * @return returns the double value of the directly distance from the starting street and avenue to the target street and avenue
	 */

	protected double calculateDistance(int startingStreet, int startingAvenue, int targetStreet, int targetAvenue) {
		int horizontalDistance = targetAvenue - startingAvenue;
		int verticalDistance = targetStreet - startingStreet;
		double distance = Math.sqrt((horizontalDistance*horizontalDistance) + (verticalDistance*verticalDistance));
		return distance;
	}

	/**
	 * Returns the ability attribute which determines the chance the medic has to successfully infect a zombie
	 * @return returns an integer from 1-100 which determines the Medic's combat ability
	 */
	@Override
	public int getCombatAbility() {
		return this.ability;
	}

	/**
	 * Returns the role of the robot, in this case "MEDIC"
	 * @return returns the String containing the role of the robot
	 */
	@Override
	public String getRole() {
		return "MEDIC";
	}
	
}
