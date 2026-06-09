package final_project_2026;

import java.util.Random;
import becker.robots.*;
import java.awt.Color;

/**
 * This is the core engine for the zombie outbreak game.
 * Handles spawning, map generation, interaction logic and the main game loop.
 * @author ayyan
 * @author adam
 * @author spencer
 * @version june 2 2026
 */
public class OutbreakApp {
    // Static variables so the main loop can read them
    private static boolean isGameOver = false;
    private static int targetThingsToWin = 15;
    private static int currentThingsCollected = 0;

    // Customizable game parameters
    private static int playerCount = 12;
    private static int startingZombieCount = 3;
    private static int maxThings;
    private static int currentThingsOnBoard = 0;
    private static final int MEDIC_SPEED = 42;

    private static GameRobot[] players = new GameRobot[playerCount];
    private static RobotInfoRecord[] records = new RobotInfoRecord[playerCount];
    private static City playground = new City(15, 26);
    private static final Random rand = new Random();

    private static int totalTurns = 0;
    /**
     * Main game entry point.
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        setupCity();
        spawnPlayers();

        int survivorCount = playerCount - startingZombieCount - 1;
        maxThings = survivorCount * 3;
        manageThings();

        System.out.println("--- outbreak game started ---");

        while (!isGameOver) {
            updateRecords();
            manageThings();
            runGameLoop();
            totalTurns++;
            checkWinConditions();     
        }
    }

    /**
     * Sets up the city walls and boundaries.
     */
    private static void setupCity() {
        // Loop to build horizontal walls
        for (int a = 0; a <= 25; a++) {
            new Wall(playground, 0, a, Direction.NORTH);
            new Wall(playground, 14, a, Direction.SOUTH);
        }

        // Loop to build vertical walls
        for (int s = 0; s <= 14; s++) {
            new Wall(playground, s, 0, Direction.WEST);
            new Wall(playground, s, 25, Direction.EAST);
        }
    }

    /**
     * Randomizes locations, speeds, abilities and instantiates all starting players.
     */
    private static void spawnPlayers() {
        // Spawn medic first at id 0
        int mStreet = generateRandomNumber(1, 13);
        int mAve = generateRandomNumber(1, 24);
        players[0] = new MedicRobot(playground, mStreet, mAve, Direction.NORTH, 0, MEDIC_SPEED, true);
        players[0].setColor(Color.WHITE);

        // Loop to spawn zombies
        for (int i = 1; i <= startingZombieCount; i++) {
            int zStreet = generateRandomNumber(1, 13);
            int zAve = generateRandomNumber(1, 24);
            int zSpeed = generateRandomNumber(1, 4);
            int zAttack = generateRandomNumber(1, 100);

            players[i] = new ZombieRobot(playground, zStreet, zAve, Direction.NORTH, i, zSpeed, zAttack, playerCount);
            players[i].setColor(Color.GREEN);
        }

        // Loop to spawn survivors
        for (int i = startingZombieCount + 1; i < playerCount; i++) {
            int sStreet = generateRandomNumber(1, 13);
            int sAve = generateRandomNumber(1, 24);
            int sSpeed = generateRandomNumber(1, 4);
            int sEvade = generateRandomNumber(26, 100);

            players[i] = new SurvivorRobot(playground, sStreet, sAve, Direction.NORTH, i, sSpeed, sEvade, 5);
            players[i].setColor(Color.ORANGE);
        }
    }

    /**
     * Manages the collection items on the board.
     */
    private static void manageThings() {
        // Check if things dropped below half
        if (currentThingsOnBoard < (maxThings / 2)) {
            int thingsToSpawn = maxThings - currentThingsOnBoard;

            for (int i = 0; i < thingsToSpawn; i++) {
                int tStreet = generateRandomNumber(1, 13);
                int tAve = generateRandomNumber(1, 24);
                new Thing(playground, tStreet, tAve);
                currentThingsOnBoard++;
            }
        }
    }

    /**
     * Updates all robot records for AI decision making.
     */
    private static void updateRecords() {
        // Loop to populate records
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null) {
                records[i] = generateRecord(players[i]);
            } else {
                records[i] = null;
            }
        }
    }

    /**
     * Main game loop that processes each player's turn.
     */
    private static void runGameLoop() {
        // Loop through each player
        for (int i = 0; i < playerCount; i++) {
            if (players[i] == null) {
                continue;
            }

            TurnAction response = players[i].takeTurn(records);
            int distanceRequested = Math.abs(response.getTargetStreet() - players[i].getStreet()) 
                                  + Math.abs(response.getTargetAvenue() - players[i].getAvenue());

            // Check to make sure the robot doesn't walk into a wall
            boolean validSpeed = distanceRequested <= players[i].getSpeed();
            boolean validBounds = response.getTargetStreet() >= 1 && response.getTargetStreet() <= 13 &&
                                response.getTargetAvenue() >= 1 && response.getTargetAvenue() <= 24;

            // Check if robot has enough speed to move AND the move is within the map limits
            if (validSpeed && validBounds) {
                executeAction(players[i], response);
            } else {
                System.out.println("Robot " + players[i].getId() + " requested an invalid move and forfeited its turn.");
            }

            records[i] = generateRecord(players[i]);
        }
    }

    /**
     * Executes physical movement and resolves combat interaction dice rolls.
     * @param bot The robot performing the action
     * @param action The instructions generated by the robot AI
     */
    private static void executeAction(GameRobot bot, TurnAction action) {
        moveRobot(bot, action.getTargetStreet(), action.getTargetAvenue());
        String intent = action.getIntent();

        // Check if intent is to pick up thing
        if (intent.equals(TurnAction.PICK_UP)) {
            if (bot.canPickThing()) {
                bot.pickThing();
                currentThingsOnBoard--;
                currentThingsCollected++;
                System.out.println("robot " + bot.getId() + " gathered a thing");
            }
        }

        // Check if intent is to infect
        if (intent.equals(TurnAction.INFECT)) {
            resolveCombat(bot, action.getTargetBot());
        }

        // Check if intent is to heal
        if (bot.getAvenue() == players[action.getTargetBot()].getAvenue() && 
            bot.getStreet() == players[action.getTargetBot()].getStreet() && 
            intent.equals(TurnAction.HEAL)) {
            System.out.println("medic healed robot " + action.getTargetBot());
            resolveCombat(bot, action.getTargetBot());
        }
    }

    /**
     * Calculates quadrants, rolls dice and determines if infection succeeds.
     * @param attacker The robot attempting the infection
     * @param targetId The ID of the robot defending
     */
    private static void resolveCombat(GameRobot attacker, int targetId) {
        GameRobot defender = players[targetId];

        // Polymorphic calls completely remove the need for instanceof checks
        int zAttackAbility = attacker.getCombatAbility();
        int sEvadeAbility = defender.getCombatAbility();

        // Determine number of dice based on quadrant
        int zDice = getQuadrant(zAttackAbility);
        int sDice = getQuadrant(sEvadeAbility);

        // Roll for highest numbers
        int zRoll = rollHighest(zDice);
        int sRoll = rollHighest(sDice);

        // Check if zombie beat defender (tie goes to defender)
        if (attacker.getRole().equals("MEDIC")) {
            if (zRoll > sRoll) {
                System.out.println("Medic won interaction " + zRoll + " vs " + sRoll);
                swapRobotClass(targetId, false);
            } else {
                System.out.println("Zombie evaded infection " + sRoll + " vs " + zRoll);
            }
        }

        if (attacker.getRole().equals("ZOMBIE")) {
            if (zRoll > sRoll) {
                System.out.println("zombie won interaction " + zRoll + " vs " + sRoll);
                swapRobotClass(targetId, true);
            } else {
                System.out.println("survivor evaded infection " + sRoll + " vs " + zRoll);
            }
        }
    }

    /**
     * Figures out which quadrant the ability falls in to determine dice count.
     * @param ability The stat from 1 to 100
     * @return Number of dice to roll
     */
    private static int getQuadrant(int ability) {
        if (ability <= 25) {
            return 1;
        }
        if (ability <= 50) {
            return 2;
        }
        if (ability <= 75) {
            return 3;
        }
        return 4;
    }

    /**
     * Rolls standard d6 dice and returns the highest result.
     * @param numDice The amount of dice to roll
     * @return The highest dice roll
     */
    private static int rollHighest(int numDice) {
        int highest = 0;
        // Loop for amount of dice
        for (int i = 0; i < numDice; i++) {
            int roll = generateRandomNumber(1, 6);
            if (roll > highest) {
                highest = roll;
            }
        }
        return highest;
    }

    /**
     * Swaps a robot's class between zombie and survivor.
     * @param targetId The ID of the robot to transform
     * @param toZombie Whether to convert to zombie (true) or survivor (false)
     */
    private static void swapRobotClass(int targetId, boolean toZombie) {
        players[targetId].setTransparency(1.0);

        int st = players[targetId].getStreet();
        int ave = players[targetId].getAvenue();
        Direction dir = players[targetId].getDirection();
        int speed = generateRandomNumber(1, 4);

        if (toZombie) {
            int attack = generateRandomNumber(1, 100);
            players[targetId] = new ZombieRobot(playground, st, ave, dir, targetId, speed, attack, playerCount);
            players[targetId].setColor(Color.GREEN);
        } else {
            int evade = generateRandomNumber(1, 100);
            players[targetId] = new SurvivorRobot(playground, st, ave, dir, targetId, speed, evade, 10);
            players[targetId].setColor(Color.ORANGE);
        }
    }

    /**
     * Moves a robot to the specified coordinates.
     * @param bot The robot to move
     * @param targetStreet Target street coordinate
     * @param targetAvenue Target avenue coordinate
     */
    private static void moveRobot(GameRobot bot, int targetStreet, int targetAvenue) {
        if (bot.getStreet() > targetStreet) {
            while (bot.getDirection() != Direction.NORTH) {
                bot.turnLeft();
            }
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        } else if (bot.getStreet() < targetStreet) {
            while (bot.getDirection() != Direction.SOUTH) {
                bot.turnLeft();
            }
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        }

        if (bot.getAvenue() > targetAvenue) {
            while (bot.getDirection() != Direction.WEST) {
                bot.turnLeft();
            }
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        } else if (bot.getAvenue() < targetAvenue) {
            while (bot.getDirection() != Direction.EAST) {
                bot.turnLeft();
            }
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        }
    }

    /**
     * Checks win conditions and ends game if met.
     */
    private static void checkWinConditions() {
        if (currentThingsCollected >= targetThingsToWin) {
            System.out.println("survivors win they collected " + currentThingsCollected + " things");
            System.out.println("won in: " + totalTurns + " turns.");
            isGameOver = true;
            return;
        }

        int survivorsLeft = 0;
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && !players[i].isZombie() && players[i].getId() != 0) {
                survivorsLeft++;
            }
        }

        if (survivorsLeft == 0) {
            System.out.println("zombies win all survivors are infected");
            System.out.println("won in: " + totalTurns + " turns.");
            isGameOver = true;
        }
    }

    /**
     * Generates a record of a robot's current state.
     * @param bot The robot to generate record for
     * @return The generated RobotInfoRecord
     */
    private static RobotInfoRecord generateRecord(GameRobot bot) {
    	if(!bot.isZombie()) {
            return new RobotInfoRecord(bot.getId(), bot.getStreet(), bot.getAvenue(), (int) bot.getSpeed(), bot.isZombie(), bot.countThingsInBackpack());
    	}
        return new RobotInfoRecord(bot.getId(), bot.getStreet(), bot.getAvenue(), (int) bot.getSpeed(), bot.isZombie(), 0);
    }

    /**
     * Generates a random number within specified bounds.
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random number between min and max
     */
    private static int generateRandomNumber(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }
}
