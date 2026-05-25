package g12CP_FinalProject;

import java.awt.Color;
import java.util.ArrayList;

import becker.robots.City;
import becker.robots.Direction;

public class MedicRobot extends GameRobot {

	public MedicRobot(City c, int st, int ave, Direction dir, int id, int speed, boolean isZombie) {
		super(c, st, ave, dir, id, speed, isZombie);
		this.hitPoints = 100;

		this.currentStrategy = "GATHER";
		this.setColor(Color.WHITE);
	}
	
	ArrayList<ZombieInfoRecord> zombieRecords = new ArrayList<ZombieInfoRecord>(); 

	private int hitPoints;

	String currentStrategy;

	/**
	 *This method uses the array of records for other robots to help decide weather it is going to heal zombies, gather things or heal itself
	 *@param state - the array of records containing information about each robot that the medic will use to make its decision
	 */
	
	@Override
	public Action takeTurn(RobotInfoRecord[] state) { 
		evaluateStrategy(state);
		Action response = determineResponse(currentStrategy, state);
		return response;
	}

	/**
	 * Depending on the strategy dictated by the evaluateStrategy method, it will calculate the nearest zombie to heal or the nearest thing to collect or move to the storing location to heal itself
	 * @param currentStrategy - The strategy dictating what the main goal of the medic's movement will be
	 * @param state - the list of records providing information on all the other robots
	 * @return - the response that the medic returns to the controller class 
	 */
	private Action determineResponse(String currentStrategy, RobotInfoRecord[] state) {
		Action response = new Action(0, 0, "");
		if (currentStrategy == "GATHER") {
			
		}
		
		// If the current strategy is heal, then make a separate array containing only the records of zombies and find the closest zombie using selection sort and return an action object requesting to heal it
		else if (currentStrategy == "HEAL") {
			
			// For the length of the records array
			for (int i = 0; i < state.length; i++) { 
				
				// If the robot is a zombie, then add its record to the zombieRecords array
				if (state[i].getIsZombie() == true) { 
					zombieRecords.add(new ZombieInfoRecord(state[i].getId(), state[i].getStreet(), state[i].getAvenue(), state[i].getSpeed(), state[i].getIsZombie(), calculateDistance(this.getStreet(), this.getAvenue() ,state[i].getStreet(), state[i].getAvenue())));
				}
			}
			
			// Use selection sort to sort the array of zombie records based on the their distance to the medic (least to greatest)
			// Outer Loop - After loop through and finding the smallest distance record swap it with the index at i and keep repeating this process for the length of zombieRecords
			for (int i = 0; i < zombieRecords.size(); i++) {
				int lastIndex = i;
				double currentMin = zombieRecords.get(i).getDistanceToMedic();
				
				// Inner loop - if the record at j has a closer distance then currentMin, then make it the new minimum and change lastIndex as well
				for (int j = i; j < state.length; j++) { 
					if (zombieRecords.get(j).getDistanceToMedic() < currentMin) { 
						lastIndex = j;
						currentMin = zombieRecords.get(j).getDistanceToMedic();
					}
				}
				ZombieInfoRecord temp = zombieRecords.get(lastIndex);
				zombieRecords.set(lastIndex, zombieRecords.get(i));
				zombieRecords.set(i, temp);
			}
			
			response = new Action(zombieRecords.get(0).getStreet(), zombieRecords.get(0).getAvenue(), "HEAL");
			
			return response;
		}
		
		return response;
	}

	
	
	
	
	// CONTINUE COMMENTING FROM HERE!!!!!
	
	
	
	
	
	private void evaluateStrategy(RobotInfoRecord[] state) {
		int survivorCount = 0;
		int zombieCount = 0;

		for (int i = 0; i < state.length; i++) {
			if (state[i].getIsZombie() == true) { 
				zombieCount += 1;
			}
			else { 
				survivorCount += 1;
			}
		}

		if (survivorCount >= zombieCount) { 
			currentStrategy = "GATHER";
		}
		else { 
			currentStrategy = "HEAL";
			
		}

	}

	@Override
	public RobotInfoRecord generateRecord() {
		return null;
	}

	@Override
	protected double calculateDistance(int startingStreet, int startingAvenue, int targetStreet, int targetAvenue) {
		int horizontalDistance = targetAvenue - startingAvenue;
		int verticalDistance = targetStreet - startingStreet;
		double distance = Math.sqrt((horizontalDistance*horizontalDistance) + (verticalDistance*verticalDistance));
		return distance;
	}
	


}
