package g12CP_FinalProject;

import becker.robots.*;

/**
 * a lightweight tester class to verify the ai logic of all three robots.
 * prints the decision making process to the console for easy debugging.
 * @author Adam
 */
public class TestSimulator {
	
	private static int playerCount = 5;
	public static City testCity = new City();

	public static void main(String[] args) {
		
		// build a mini 10x10 arena for quick encounters
		for(int i = 0; i <= 10; i++) {
			new Wall(testCity, i, 0, Direction.WEST);
			new Wall(testCity, i, 10, Direction.EAST);
			new Wall(testCity, 0, i, Direction.NORTH);
			new Wall(testCity, 10, i, Direction.SOUTH);
		}
		
		// spawn one of each robot type
		GameRobot[] players = new GameRobot[playerCount];
		players[0] = new MedicRobot(testCity, 9, 9, Direction.NORTH, 0, 2, false); // id 1
		players[1] = new ZombieRobot(testCity, 1, 1, Direction.SOUTH, 1, 3); // id 2
		players[2] = new ZombieRobot(testCity, 2, 3, Direction.SOUTH, 2, 3); // id 2
		players[3] = new ZombieRobot(testCity, 4, 5, Direction.SOUTH, 3, 3); // id 2
		players[4] = new ZombieRobot(testCity, 1, 5, Direction.SOUTH, 4, 3); // id 2
//		players[4] = new SurvivorRobot(testCity, 5, 7, Direction.EAST, 4, 2, 1); // id 3
		
		// drop a test thing for the survivor/medic to find
		new Thing(testCity, 5, 7);
		
		RobotInfoRecord[] records = new RobotInfoRecord[playerCount];
		
		System.out.println("--- starting ai logic test ---");
		
		// 1. generate fresh records
		for(int i = 0; i < playerCount; i++) {
			records[i] = players[i].generateRecord();
		}
		
		
//		// run a quick 10-turn test loop
		for (int turn = 1; turn <= 2; turn++) {
			System.out.println("\n--- turn " + turn + " ---");
			
			// 2. prompt each robot and print their brain's output
//			for(int i = 0; i < playerCount; i++) {
//				TurnAction action = players[i].takeTurn(records);
				
				runGameLoop(players, records);
				
				
				
//				// 3. execute the movement if valid
//				if(action.getIntent().equals("MOVE") || action.getIntent().equals("INFECT")) {
//					moveRobot(players[i], action.getTargetStreet(), action.getTargetAvenue());
//				}
		}
		System.out.println("\n--- test loop complete ---");
	}
	
	private static void runGameLoop(GameRobot[] players, RobotInfoRecord[] records) {
		// A loop is ran for the length of the players array, prompting each player to take their turn
		for (int i = 0; i < playerCount; i++) { 
			// The loop continues to run until the win/loss conditions are met
				TurnAction response = players[i].takeTurn(records);
				if (response.getIntent() == "MOVE") {
					players[i].move();
				}  
				
				if (response.getIntent() == "HEAL") { 
					int targetId = 0;
					moveRobot(players[i], response.getTargetStreet(), response.getTargetAvenue());
					for (int j = 0; j < players.length; j++) { 
						if (players[j].getId() == response.getTargetBot()) {
							targetId = players[j].getId();
						}
					}
					
					System.out.print("target ID is " + targetId);
					players[targetId].setTransparency(1);
					players[targetId] = new SurvivorRobot(testCity, players[targetId].getStreet(), players[targetId].getAvenue(), players[targetId].getDirection(), targetId, 1, 1);
				}
				
				// print what the ai decided to do to the console
				System.out.println("robot id " + players[i].getId() + 
						" wants to " + response.getIntent() + 
						" to (" + response.getTargetStreet() + ", " + response.getTargetAvenue() + ")");
				
				
//				int totalSteps = response.getTargetAvenue() + response.getTargetStreet();
				
				// Validation check, if the player does not have enough speed, then the move will not executed
//				if (totalSteps <= players[i].getSpeed()) {
//					continue;
//				}
				
				records[i] = players[i].generateRecord();
				System.out.println("avenue:" + records[i].getAvenue() + "  Street:" + records[i].getStreet() + " isZombie" + records[i].getIsZombie());
				
		}
	}

	/**
	 * physically turns and moves the becker robot to the target coordinates
	 * @param bot the robot to move
	 * @param targetStreet the destination street
	 * @param targetAvenue the destination avenue
	 */
	private static void moveRobot(GameRobot bot, int targetStreet, int targetAvenue) {
		// move vertically first
		if (bot.getStreet() > targetStreet) {
			while (bot.getDirection() != Direction.NORTH) bot.turnLeft();
			while (bot.getStreet() != targetStreet) bot.move();
		} else if (bot.getStreet() < targetStreet) {
			while (bot.getDirection() != Direction.SOUTH) bot.turnLeft();
			while (bot.getStreet() != targetStreet) bot.move();
		}
		
		// move horizontally second
		if (bot.getAvenue() > targetAvenue) {
			while (bot.getDirection() != Direction.WEST) bot.turnLeft();
			while (bot.getAvenue() != targetAvenue) bot.move();
		} else if (bot.getAvenue() < targetAvenue) {
			while (bot.getDirection() != Direction.EAST) bot.turnLeft();
			while (bot.getAvenue() != targetAvenue) bot.move();
		}
	}
}
