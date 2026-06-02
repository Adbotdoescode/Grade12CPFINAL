package final_project_2026;

import becker.robots.*;
import java.awt.Color;
import java.util.Scanner;

public class TestZombie {

    /**
     * main method to run all zombie test cases sequentially
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        System.out.println("--- starting zombie testing suite ---");
        
        System.out.println("\n--- test 1 distance targeting ---");
        testDistanceTargeting(input);
        
        System.out.println("\n--- test 2 item targeting ---");
        testItemTargeting(input);
        
        System.out.println("\n--- test 3 evasion targeting ---");
        testEvasionLearning(input);
        
        System.out.println("\n--- test 4 infection and healing mechanics ---");
        testInfectionAndHealing(input);
        
        System.out.println("\n--- testing suite complete ---");
        input.close();
    }

    /**
     * helper method to pause the console and wait for user input
     * @param input the scanner object reading the console
     * @param message the prompt to display to the user
     */
    private static void pause(Scanner input, String message) {
        System.out.println(message);
        input.nextLine();
    }

    /**
     * physically moves the robot on the becker grid to the target location
     * @param bot the robot to move
     * @param targetStreet the street to move to
     * @param targetAvenue the avenue to move to
     */
    private static void executeMove(GameRobot bot, int targetStreet, int targetAvenue) {
        // handle vertical street movement first
        if (bot.getStreet() > targetStreet) {
            // loop to face north
            while (bot.getDirection() != Direction.NORTH) {
                bot.turnLeft();
            }
            // loop to drive forward
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        } else if (bot.getStreet() < targetStreet) {
            // loop to face south
            while (bot.getDirection() != Direction.SOUTH) {
                bot.turnLeft();
            }
            // loop to drive forward
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        }

        // handle horizontal avenue movement next
        if (bot.getAvenue() > targetAvenue) {
            // loop to face west
            while (bot.getDirection() != Direction.WEST) {
                bot.turnLeft();
            }
            // loop to drive forward
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        } else if (bot.getAvenue() < targetAvenue) {
            // loop to face east
            while (bot.getDirection() != Direction.EAST) {
                bot.turnLeft();
            }
            // loop to drive forward
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        }
    }

    /**
     * tests if zombie correctly targets and moves to the closest survivor visually
     * @param input scanner to pause execution
     */
    public static void testDistanceTargeting(Scanner input) {
        City city = new City();
        ZombieRobot zombie = new ZombieRobot(city, 5, 5, Direction.NORTH, 0, 3);
        zombie.setColor(Color.GREEN);
        
        // spawn visual dummies so you can see the layout
        SurvivorRobot s1 = new SurvivorRobot(city, 5, 6, Direction.NORTH, 1, 2, 1, 10);
        s1.setColor(Color.ORANGE);
        SurvivorRobot s2 = new SurvivorRobot(city, 5, 10, Direction.NORTH, 2, 2, 1, 10);
        s2.setColor(Color.ORANGE);
        
        // craft manual records to match visual layout
        RobotInfoRecord[] state = new RobotInfoRecord[3];
        state[0] = zombie.generateRecord();
        state[1] = new RobotInfoRecord(1, 5, 6, 2, false, 0); 
        state[2] = new RobotInfoRecord(2, 5, 10, 2, false, 0);
        
        TurnAction action = zombie.takeTurn(state);
        System.out.println("expected target 1 (closer)");
        System.out.println("actual target " + action.getTargetBot());
        
        // execute the physical movement
        executeMove(zombie, action.getTargetStreet(), action.getTargetAvenue());
        
        pause(input, "press enter to continue to test 2");
    }

    /**
     * tests if zombie targets and moves to survivor with more items when distances are equal
     * @param input scanner to pause execution
     */
    public static void testItemTargeting(Scanner input) {
        City city = new City();
        ZombieRobot zombie = new ZombieRobot(city, 5, 5, Direction.NORTH, 0, 3);
        zombie.setColor(Color.GREEN);
        
        // spawn visual dummies equally distant from zombie
        SurvivorRobot s1 = new SurvivorRobot(city, 5, 7, Direction.NORTH, 1, 2, 1, 10);
        s1.setColor(Color.ORANGE);
        SurvivorRobot s2 = new SurvivorRobot(city, 5, 3, Direction.NORTH, 2, 2, 1, 10);
        s2.setColor(Color.ORANGE);
        
        // craft manual records to inject 5 items into bot 2
        RobotInfoRecord[] state = new RobotInfoRecord[3];
        state[0] = zombie.generateRecord();
        state[1] = new RobotInfoRecord(1, 5, 7, 2, false, 0); 
        state[2] = new RobotInfoRecord(2, 5, 3, 2, false, 5); 
        
        TurnAction action = zombie.takeTurn(state);
        System.out.println("expected target 2 (items subtract from threat score)");
        System.out.println("actual target " + action.getTargetBot());
        
        // execute the physical movement
        executeMove(zombie, action.getTargetStreet(), action.getTargetAvenue());
        
        pause(input, "press enter to continue to test 3");
    }

    /**
     * tests if zombie switches targets and moves after a survivor successfully evades
     * @param input scanner to pause execution
     */
    public static void testEvasionLearning(Scanner input) {
        City city = new City();
        ZombieRobot zombie = new ZombieRobot(city, 5, 5, Direction.NORTH, 0, 3);
        zombie.setColor(Color.GREEN);
        
        // visual dummies placed at their final post evasion locations
        SurvivorRobot s1 = new SurvivorRobot(city, 5, 7, Direction.NORTH, 1, 2, 1, 10);
        s1.setColor(Color.ORANGE);
        SurvivorRobot s2 = new SurvivorRobot(city, 5, 3, Direction.NORTH, 2, 2, 1, 10);
        s2.setColor(Color.ORANGE);
        
        RobotInfoRecord[] state = new RobotInfoRecord[3];
        state[0] = zombie.generateRecord();
        
        // turn 1 simulate bot 1 being closer initially
        state[1] = new RobotInfoRecord(1, 5, 6, 2, false, 0);
        state[2] = new RobotInfoRecord(2, 5, 10, 2, false, 0);
        zombie.takeTurn(state);
        System.out.println("turn 1 targeted closest bot (bot 1)");
        
        // turn 2 simulate bot 1 escaping now both are equally distant
        state[1] = new RobotInfoRecord(1, 5, 7, 2, false, 0); 
        state[2] = new RobotInfoRecord(2, 5, 3, 2, false, 0);
        
        TurnAction action = zombie.takeTurn(state);
        System.out.println("expected target 2 (bot 1 has evasion points adding to threat score)");
        System.out.println("actual target " + action.getTargetBot());
        
        // execute the physical movement
        executeMove(zombie, action.getTargetStreet(), action.getTargetAvenue());
        
        pause(input, "press enter to continue to test 4");
    }

    /**
     * tests the visual mechanics of infecting and healing robots
     * @param input scanner to pause execution
     */
    public static void testInfectionAndHealing(Scanner input) {
        City city = new City();
        GameRobot[] players = new GameRobot[3];
        
        players[0] = new MedicRobot(city, 1, 1, Direction.SOUTH, 0, 2, false);
        players[0].setColor(Color.WHITE);
        players[1] = new ZombieRobot(city, 4, 4, Direction.NORTH, 1, 3);
        players[1].setColor(Color.GREEN);
        players[2] = new SurvivorRobot(city, 4, 5, Direction.NORTH, 2, 2, 1, 10);
        players[2].setColor(Color.ORANGE);
        
        RobotInfoRecord[] state = new RobotInfoRecord[3];
        for (int i = 0; i < players.length; i++) {
            state[i] = players[i].generateRecord();
        }
        
        pause(input, "initial state loaded press enter to run zombie infect turn");
        
        // step 1 zombie targets and infects survivor
        TurnAction zombieAction = players[1].takeTurn(state);
        if (zombieAction.getIntent().equals(TurnAction.INFECT)) {
            System.out.println("zombie infected bot " + zombieAction.getTargetBot());
            // physically move zombie to target
            executeMove(players[1], zombieAction.getTargetStreet(), zombieAction.getTargetAvenue());
            swapRobotClass(city, players, zombieAction.getTargetBot(), true);
        }
        
        pause(input, "survivor infected press enter to run medic heal turn");
        
        // update state array after infection so medic knows
        for (int i = 0; i < players.length; i++) {
            state[i] = players[i].generateRecord();
        }
        
        // step 2 medic targets and heals new zombie
        TurnAction medicAction = players[0].takeTurn(state);
        if (medicAction.getIntent().equals(TurnAction.HEAL)) {
            System.out.println("medic healed bot " + medicAction.getTargetBot());
            // physically move medic to target
            executeMove(players[0], medicAction.getTargetStreet(), medicAction.getTargetAvenue());
            swapRobotClass(city, players, medicAction.getTargetBot(), false);
        }
        
        pause(input, "medic healed press enter to finish");
    }

    /**
     * helper method to handle robot death and revival visually
     * @param city the game map
     * @param players the array of active players
     * @param targetId the id of the robot being converted
     * @param toZombie boolean true if becoming zombie false if survivor
     */
    private static void swapRobotClass(City city, GameRobot[] players, int targetId, boolean toZombie) {
        // loop through array to find target
        for (int i = 0; i < players.length; i++) {
            // check if robot is the target
            if (players[i] != null && players[i].getId() == targetId) {
                // make old robot transparent
                players[i].setTransparency(1.0);
                
                int st = players[i].getStreet();
                int ave = players[i].getAvenue();
                Direction dir = players[i].getDirection();
                
                // replace with new class based on boolean flag
                if (toZombie) {
                    players[i] = new ZombieRobot(city, st, ave, dir, targetId, 3);
                    players[i].setColor(Color.GREEN);
                    System.out.println("bot " + targetId + " converted to zombie");
                } else {
                    players[i] = new SurvivorRobot(city, st, ave, dir, targetId, 2, 1, 10);
                    players[i].setColor(Color.ORANGE);
                    System.out.println("bot " + targetId + " converted to survivor");
                }
            }
        }
    }
}