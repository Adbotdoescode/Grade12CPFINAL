package final_project_2026;

import java.util.ArrayList;
import java.util.Random;  
import becker.robots.*;
import java.awt.Color;

/**
 * this is the core engine for the zombie outbreak game
 * handles spawning map generation interaction logic and the main game loop
 * @author ayyan
 * @author adam
 * @author spencer
 * @version june 2 2026
 */
public class OutbreakApp {

	// static variables for game state tracking
	private static boolean isGameOver = false;
	private static int targetThingsToWin = 25;
	private static int currentThingsCollected = 0;
	private static int totalTurns = 0;

	// customizable game parameters
	private static int playerCount = 17;
	private static int startingZombieCount = 5;
	private static int maxThings;
	private static int currentThingsOnBoard = 0;
	private static final int MEDIC_SPEED = 4;


	// variables for random generation and memory lists
	private static final Random rand = new Random();
	public static ArrayList<ZombieInfoRecord> zombieRecords; 

	// array variables holding game entities
	private static GameRobot[] players = new GameRobot[playerCount];
	private static RobotInfoRecord[] records = new RobotInfoRecord[playerCount];
	private static City playground = new City(15, 26);

	/**
	 * main entry point for the game application
	 * @param args command line arguments
	 */
	public static void main(String[] args) { 
		setupCity();
		spawnPlayers();

		// variables to determine item spawn limits
		int survivorCount = playerCount - startingZombieCount - 1; 
		maxThings = survivorCount * 3;
		manageThings();

		System.out.println("--- outbreak game started ---");

		// loop to run game phases while win condition is unmet
		while (!isGameOver) { 
			updateRecords();
			manageThings();
			runGameLoop();
			totalTurns++;
			checkWinConditions();
		}
	}

	/**
	 * sets up the visual boundaries and walls of the city
	 */
	private static void setupCity() {
		// loop to build horizontal boundary walls
		for (int a = 0; a <= 25; a++) {
			new Wall(playground, 0, a, Direction.NORTH);
			new Wall(playground, 14, a, Direction.SOUTH);
		}

		// loop to build vertical boundary walls
		for (int s = 0; s <= 14; s++) {
			new Wall(playground, s, 0, Direction.WEST);
			new Wall(playground, s, 25, Direction.EAST);
		}
	}

	/**
	 * randomizes locations speeds abilities and instantiates all starting players
	 */
	private static void spawnPlayers() {
		// spawn medic first at id 0
		int mStreet = generateRandomNumber(1, 13);
		int mAve = generateRandomNumber(1, 24);
		players[0] = new MedicRobot(playground, mStreet, mAve, Direction.NORTH, 0, MEDIC_SPEED, true);
		players[0].setColor(Color.WHITE);


		// loop to spawn zombies with random stats
		for (int i = 1; i <= startingZombieCount; i++) {
			// variables for zombie spawn logic
			int zStreet = generateRandomNumber(1, 13);
			int zAve = generateRandomNumber(1, 24);
			int zSpeed = generateRandomNumber(1, 5);
			int zAttack = generateRandomNumber(1, 100);

			players[i] = new ZombieRobot(playground, zStreet, zAve, Direction.NORTH, i, zSpeed, zAttack, playerCount);
			players[i].setColor(Color.GREEN);
		}

		// loop to spawn survivors with random stats
		for (int i = startingZombieCount + 1; i < playerCount; i++) {
			// variables for survivor spawn logic
			int sStreet = generateRandomNumber(1, 13);
			int sAve = generateRandomNumber(1, 24);
			int sSpeed = generateRandomNumber(1, 5);
			int sEvade = generateRandomNumber(1, 100);

			players[i] = new SurvivorRobot(playground, sStreet, sAve, Direction.NORTH, i, sSpeed, sEvade);
			players[i].setColor(Color.ORANGE);
		}
	}

	/**
	 * checks current item counts and spawns new items if necessary
	 */
	private static void manageThings() {
		// check if things dropped below half capacity
		if (currentThingsOnBoard < (maxThings / 2)) {
			// variable for amount of items needed
			int thingsToSpawn = maxThings - currentThingsOnBoard;

			// loop to spawn individual items
			for (int i = 0; i < thingsToSpawn; i++) {
				// variables for item location
				int tStreet = generateRandomNumber(1, 13);
				int tAve = generateRandomNumber(1, 24);
				new Thing(playground, tStreet, tAve);
				currentThingsOnBoard++;
			}
		}
	}

	/**
	 * updates all robot records for ai decision making
	 */
	private static void updateRecords() {
		// loop to populate records array
		for (int i = 0; i < playerCount; i++) {
			// check if player exists
			if (players[i] != null) {
				records[i] = generateRecord(players[i]);
			}
			// fallback if player slot is empty
			else {
				records[i] = null;
			}
		}
	}

	/**
	 * processes turns and requested actions for all valid players
	 */
	private static void runGameLoop() {
		// loop to process each player turn
		for (int i = 0; i < playerCount; i++) { 
			// check if player slot is empty
			if (players[i] == null) {
				continue;
			}

			// variable to hold turn instructions
			TurnAction response;

			// check if current player is medic to pass specific records
			if (players[i].getRole().equals("MEDIC")) {
				zombieRecords = new ArrayList<ZombieInfoRecord>();
				response = players[i].takeTurn(records, zombieRecords);
			}
			// fallback for all other robot types
			else { 
				response = players[i].takeTurn(records);
			}

			// variable to calculate total distance requested by ai
			int distanceRequested = Math.abs(response.getTargetStreet() - players[i].getStreet()) 
					+ Math.abs(response.getTargetAvenue() - players[i].getAvenue());

			// check if robot has enough speed to cover requested distance
			if (distanceRequested <= players[i].getSpeed()) {
				executeAction(players[i], response);
			}

			records[i] = generateRecord(players[i]);
		}
	}

	/**
	 * executes physical movement and resolves interaction intents
	 * @param bot the robot performing the action
	 * @param action the instructions generated by the ai
	 */
	private static void executeAction(GameRobot bot, TurnAction action) {
		moveRobot(bot, action.getTargetStreet(), action.getTargetAvenue());

		// variable holding intended interaction
		String intent = action.getIntent();

		// check if intent is to gather item
		if (intent.equals(TurnAction.PICK_UP)) {
			// check if item exists on tile
			if (bot.canPickThing()) {
				bot.pickThing();
				currentThingsOnBoard--;
				currentThingsCollected++;
				System.out.println("robot " + bot.getId() + " gathered a thing");
			}
		}

		// check if intent is combat interaction
		if (intent.equals(TurnAction.INFECT)) {
			resolveCombat(bot, action.getTargetBot());
		}

		// check if intent is healing and robots occupy same tile
		if (bot.getAvenue() == players[action.getTargetBot()].getAvenue() && bot.getStreet() == players[action.getTargetBot()].getStreet() && intent.equals(TurnAction.HEAL)) {
			System.out.println("medic healed robot " + action.getTargetBot());
			resolveCombat(bot, action.getTargetBot());
		}
	}

	/**
	 * calculates quadrants rolls dice and determines combat outcome
	 * @param attacker the robot initiating combat
	 * @param targetId the id of the robot defending
	 */
	private static void resolveCombat(GameRobot attacker, int targetId) {
		// variable referencing defending robot
		GameRobot defender = players[targetId];

		// variables holding raw combat stats
		int zAttackAbility = attacker.getCombatAbility();
		int sEvadeAbility = defender.getCombatAbility();

		// variables holding dice pool count
		int zDice = getQuadrant(zAttackAbility);
		int sDice = getQuadrant(sEvadeAbility);

		// variables holding highest rolled values
		int zRoll = rollHighest(zDice);
		int sRoll = rollHighest(sDice);

		// check if attacker is a medic robot
		if (attacker.getRole().equals("MEDIC")) {
			// check if medic roll beat defender roll
			if (zRoll > sRoll) {
				System.out.println("medic won interaction " + zRoll + " vs " + sRoll);
				swapRobotClass(targetId, false);
			}
			// fallback if medic lost roll
			else {
				System.out.println("zombie evaded infection " + sRoll + " vs " + zRoll);
			}
		}

		// check if attacker is a zombie robot
		if (attacker.getRole().equals("ZOMBIE")) {
			// check if zombie roll beat defender roll
			if (zRoll > sRoll) {
				System.out.println("zombie won interaction " + zRoll + " vs " + sRoll);
				swapRobotClass(targetId, true);
			} 
			// fallback if zombie lost roll
			else {
				System.out.println("survivor evaded infection " + sRoll + " vs " + zRoll);

				// check if defender is survivor to update dodge memory
				if (defender.getRole() == "SURVIVOR") {
					((SurvivorRobot)defender).registerSuccessfulDodge();
				}
			}
		}
	}

	/**
	 * determines dice pool size based on stat grouping
	 * @param ability the stat from 1 to 100
	 * @return number of dice to roll
	 */
	private static int getQuadrant(int ability) {
		// check if ability falls in lowest quarter
		if (ability <= 25) { 
			return 1; 
		}
		// check if ability falls in second quarter
		if (ability <= 50) { 
			return 2; 
		}
		// check if ability falls in third quarter
		if (ability <= 75) { 
			return 3; 
		}
		return 4;
	}

	/**
	 * rolls standard d6 dice and returns highest single result
	 * @param numDice the amount of dice to roll
	 * @return the highest dice roll
	 */
	private static int rollHighest(int numDice) {
		// variable holding highest tracked number
		int highest = 0;

		// loop to roll dice requested amount of times
		for (int i = 0; i < numDice; i++) {
			// variable holding current roll
			int roll = generateRandomNumber(1, 6);

			// check if current roll exceeds tracked highest
			if (roll > highest) {
				highest = roll;
			}
		}
		return highest;
	}

	/**
	 * replaces existing robot object with converted instance and deducts items
	 * @param targetId id of robot to swap
	 * @param toZombie dictates which class to spawn
	 */
	private static void swapRobotClass(int targetId, boolean toZombie) {
		// check if survivor is dying to deduct items from global score
		if (toZombie && !players[targetId].isZombie()) {
			// variable storing items lost on death
			int lostItems = players[targetId].countThingsInBackpack();
			currentThingsCollected -= lostItems;
			System.out.println("a survivor was infected " + lostItems + " items were lost from the team total");
		}

		players[targetId].setTransparency(1.0);

		// variables preserving target location and stats
		int st = players[targetId].getStreet();
		int ave = players[targetId].getAvenue();
		Direction dir = players[targetId].getDirection();
		int speed = generateRandomNumber(1, 4);

		// check if target becomes zombie
		if (toZombie) {
			// variable to generate random attack stat
			int attack = generateRandomNumber(1, 100);
			players[targetId] = new ZombieRobot(playground, st, ave, dir, targetId, speed, attack, playerCount);
			players[targetId].setColor(Color.GREEN);
		} 
		// fallback if target becomes survivor
		else {
			// variable to generate random evade stat
			int evade = generateRandomNumber(1, 100);
			players[targetId] = new SurvivorRobot(playground, st, ave, dir, targetId, speed, evade);
			players[targetId].setColor(Color.ORANGE);
		}
	}

	/**
	 * controls physical movement instructions to reach coordinates
	 * @param bot robot to move
	 * @param targetStreet destination street
	 * @param targetAvenue destination avenue
	 */
	private static void moveRobot(GameRobot bot, int targetStreet, int targetAvenue) {
		// check if target is north
		if (bot.getStreet() > targetStreet) {
			// loop to orient bot north
			while (bot.getDirection() != Direction.NORTH) { 
				bot.turnLeft(); 
			}
			// loop to step forward
			while (bot.getStreet() != targetStreet) { 
				bot.move(); 
			}
		} 
		// check if target is south
		else if (bot.getStreet() < targetStreet) {
			// loop to orient bot south
			while (bot.getDirection() != Direction.SOUTH) { 
				bot.turnLeft(); 
			}
			// loop to step forward
			while (bot.getStreet() != targetStreet) { 
				bot.move(); 
			}
		}

		// check if target is west
		if (bot.getAvenue() > targetAvenue) {
			// loop to orient bot west
			while (bot.getDirection() != Direction.WEST) { 
				bot.turnLeft(); 
			}
			// loop to step forward
			while (bot.getAvenue() != targetAvenue) { 
				bot.move(); 
			}
		} 
		// check if target is east
		else if (bot.getAvenue() < targetAvenue) {
			// loop to orient bot east
			while (bot.getDirection() != Direction.EAST) { 
				bot.turnLeft(); 
			}
			// loop to step forward
			while (bot.getAvenue() != targetAvenue) { 
				bot.move(); 
			}
		}
	}

	/**
	 * checks game states to end main loop
	 */
	private static void checkWinConditions() {
		// check if survivor collection goal is met
		if (currentThingsCollected >= targetThingsToWin) {
			System.out.println("survivors win in " + totalTurns + " turns they collected " + currentThingsCollected + " things");
			isGameOver = true;
			return;
		}

		// variable to track remaining survivors
		int survivorsLeft = 0;

		// loop to search array for remaining survivors
		for (int i = 0; i < playerCount; i++) {
			// check if entity is valid active survivor
			if (players[i] != null && !players[i].isZombie() && players[i].getId() != 0) {
				survivorsLeft++;
			}
		}

		// check if all survivors have been infected
		if (survivorsLeft == 0) {
			System.out.println("zombies win in " + totalTurns + " turns all survivors are infected");
			isGameOver = true;
		}
	}

	/**
	 * generates read only records for the state array
	 * @param bot robot to convert to record
	 * @return constructed info record
	 */
	private static RobotInfoRecord generateRecord(GameRobot bot) {
		// check if robot is survivor
		if(!bot.isZombie()) {
			// variable to calculate speed penalty from items
			int dynamicSurvivorSpeed = (int) Math.max(1, bot.getSpeed() - bot.countThingsInBackpack());
			return new RobotInfoRecord(bot.getId(), bot.getStreet(), bot.getAvenue(), dynamicSurvivorSpeed, bot.isZombie(), bot.countThingsInBackpack());
		}
		// fallback for medic and zombies
		else {
			return new RobotInfoRecord(bot.getId(), bot.getStreet(), bot.getAvenue(), (int) bot.getSpeed(), bot.isZombie(), 0);
		}
	}

	/**
	 * generates random numbers
	 * @param min lower boundary
	 * @param max upper boundary
	 * @return integer between minimum and maximum bounds
	 */
	private static int generateRandomNumber(int min, int max) {
		return rand.nextInt(max - min + 1) + min;
	}
}
