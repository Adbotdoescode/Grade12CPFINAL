package final_project_2026;

import becker.robots.*;

/**
 * a lightweight tester class to verify the ai logic of all three robots.
 * prints the decision making process to the console for easy debugging.
 * @author Adam
 */
public class TestSimulator {

	public static void main(String[] args) {
		City testCity = new City();
		
		// build a mini 10x10 arena for quick encounters
		for(int i = 0; i <= 10; i++) {
			new Wall(testCity, i, 0, Direction.WEST);
			new Wall(testCity, i, 10, Direction.EAST);
			new Wall(testCity, 0, i, Direction.NORTH);
			new Wall(testCity, 10, i, Direction.SOUTH);
		}
		
		// spawn one of each robot type
		GameRobot[] players = new GameRobot[3];
		players[0] = new MedicRobot(testCity, 9, 9, Direction.NORTH, 1, 2); // id 1
		players[1] = new ZombieRobot(testCity, 1, 1, Direction.SOUTH, 2, 3); // id 2
		players[2] = new SurvivorRobot(testCity, 5, 5, Direction.EAST, 3, 2); // id 3
		
		// drop a test thing for the survivor/medic to find
		new Thing(testCity, 5, 7);
		
		RobotInfoRecord[] records = new RobotInfoRecord[3];
		
		System.out.println("--- starting ai logic test ---");
		
		// run a quick 10-turn test loop
		for(int turn = 1; turn <= 10; turn++) {
			System.out.println("\n--- turn " + turn + " ---");
			
			// 1. generate fresh records
			for(int i = 0; i < 3; i++) {
				records[i] = players[i].generateRecord();
			}
			
			// 2. prompt each robot and print their brain's output
			for(int i = 0; i < 3; i++) {
				TurnAction action = players[i].takeTurn(records);
				
				// print what the ai decided to do to the console
				System.out.println("robot id " + players[i].getId() + 
						" wants to " + action.getIntent() + 
						" to (" + action.getTargetStreet() + ", " + action.getTargetAvenue() + ")");
				
				// 3. execute the movement if valid
				if(action.getIntent().equals("MOVE") || action.getIntent().equals("INFECT")) {
					moveRobot(players[i], action.getTargetStreet(), action.getTargetAvenue());
				}
			}
		}
		System.out.println("\n--- test loop complete ---");
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
