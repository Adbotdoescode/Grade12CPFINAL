package g12CP_FinalProject;

import becker.robots.City;

/**
 * This is the application class for the game. It prompts each player to take a move, validates the move and does it if valid. It acts as a supervisor for the whole game
 * @author ayyan
 * @version May 25 2026
 */
public class OutbreakApp {
	
	// Initialize instance variables
	private boolean isGameOver;
	private int targetThingsToWin;
	private int currentThingsCollected;
	private static int playerCount = 2;
	private static GameRobot[] players = new GameRobot[playerCount];
	private static RobotInfoRecord[] records = new RobotInfoRecord[playerCount];
	
	/**
	 * This main method continues to run the entire game (each player takes a turn) until either all the survivors die or the survivors collect the necessary amount of things
	 * @param args
	 */
	public static void main(String[] args) { 
		setupCity();
		
		// The game will continue to run until the win/loss conditions are met
		while (true) { 
			runGameLoop();
			checkWinConditions();
		}
	}
	
	
	private static void checkWinConditions() {
		
	}


	/**
	 * Each players take turn method is called so they can return their request which is verified, then executed
	 */
	private static void runGameLoop() {
		// A loop is ran for the length of the players array, prompting each player to take their turn
		for (int i = 0; i < playerCount; i++) { 
			// The loop continues to run until the win/loss conditions are met
				TurnAction response = players[i].takeTurn(records);
				int totalSteps = response.getTargetAvenue() + response.getTargetStreet();
				
				// Validation check, if the player does not have enough speed, then the move will not executed
				if (totalSteps <= players[i].getSpeed()) {
					continue;
				}
				
			records[i] = players[i].generateRecord();
		}
	}


	private static void setupCity() {
	City playground = new City();
	}


	
}
