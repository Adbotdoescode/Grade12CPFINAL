package final_project_2026;

import becker.robots.*;

public class TestSimulator {
    
    private static int playerCount = 6;
    public static City testCity = new City();

    public static void main(String[] args) {
        
        // loop to build a mini 10x10 arena
        for(int i = 0; i <= 10; i++) {
            new Wall(testCity, i, 0, Direction.WEST);
            new Wall(testCity, i, 10, Direction.EAST);
            new Wall(testCity, 0, i, Direction.NORTH);
            new Wall(testCity, 10, i, Direction.SOUTH);
        }
        
        GameRobot[] players = new GameRobot[playerCount];
        
        players[0] = new MedicRobot(testCity, 9, 9, Direction.NORTH, 0, 2, false); 
        players[1] = new ZombieRobot(testCity, 1, 1, Direction.SOUTH, 1, 3); 
        players[2] = new ZombieRobot(testCity, 2, 3, Direction.SOUTH, 2, 3); 
        players[3] = new SurvivorRobot(testCity, 4, 5, Direction.SOUTH, 3, 2, 1, 10);
        players[4] = new SurvivorRobot(testCity, 1, 5, Direction.SOUTH, 4, 2, 1, 10);
        players[5] = new ZombieRobot(testCity, 8, 8, Direction.NORTH, 5, 3);
        
        new Thing(testCity, 5, 5);
        new Thing(testCity, 2, 5);
        
        RobotInfoRecord[] records = new RobotInfoRecord[playerCount];
        
        System.out.println("--- starting ai logic test ---");
        
        // loop to generate fresh records
        for(int i = 0; i < playerCount; i++) {
            records[i] = players[i].generateRecord();
        }
        
        // loop for quick test run
        for (int turn = 1; turn <= 5; turn++) {
            System.out.println("\n--- turn " + turn + " ---");
            runGameLoop(players, records);
        }
        System.out.println("\n--- test loop complete ---");
    }
    
    private static void runGameLoop(GameRobot[] players, RobotInfoRecord[] records) {
        // loop for prompting each player turn
        for (int i = 0; i < playerCount; i++) { 
            TurnAction response = players[i].takeTurn(records);
            
            // check if response intent is move
            if (response.getIntent().equals("MOVE")) {
                moveRobot(players[i], response.getTargetStreet(), response.getTargetAvenue());
            }  
            
            // check if response intent is heal
            if (response.getIntent().equals("HEAL")) { 
                int targetId = 0;
                moveRobot(players[i], response.getTargetStreet(), response.getTargetAvenue());
                
                // loop through players to find target
                for (int j = 0; j < players.length; j++) { 
                    // check if player id matches target id
                    if (players[j].getId() == response.getTargetBot()) {
                        targetId = players[j].getId();
                    }
                }
                
                System.out.println("target ID is " + targetId);
                players[targetId].setTransparency(1);
                players[targetId] = new SurvivorRobot(testCity, players[targetId].getStreet(), players[targetId].getAvenue(), players[targetId].getDirection(), targetId, 1, 1, 10);
            }
            
            // check if response intent is infect
            if (response.getIntent().equals("INFECT")) {
                int targetId = 0;
                moveRobot(players[i], response.getTargetStreet(), response.getTargetAvenue());
                
                // loop through players to find target
                for (int j = 0; j < players.length; j++) {
                    // check if player id matches target id
                    if (players[j].getId() == response.getTargetBot()) {
                        targetId = players[j].getId();
                    }
                }
                
                System.out.println("infected target ID is " + targetId);
                players[targetId].setTransparency(1);
                players[targetId] = new ZombieRobot(testCity, players[targetId].getStreet(), players[targetId].getAvenue(), players[targetId].getDirection(), targetId, 3);
            }
            
            // check if response intent is pick up
            if (response.getIntent().equals("PICK_UP")) {
                moveRobot(players[i], response.getTargetStreet(), response.getTargetAvenue());
                // check if robot can pick thing
                if (players[i].canPickThing()) {
                    players[i].pickThing();
                }
            }
            
            System.out.println("robot id " + players[i].getId() + 
                    " wants to " + response.getIntent() + 
                    " to (" + response.getTargetStreet() + ", " + response.getTargetAvenue() + ")");
            
            records[i] = players[i].generateRecord();
        }
    }

    private static void moveRobot(GameRobot bot, int targetStreet, int targetAvenue) {
        // check if bot street is greater than target street
        if (bot.getStreet() > targetStreet) {
            // loop to face north
            while (bot.getDirection() != Direction.NORTH) {
                bot.turnLeft();
            }
            // loop to move to target street
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        } 
        // check if bot street is less than target street
        else if (bot.getStreet() < targetStreet) {
            // loop to face south
            while (bot.getDirection() != Direction.SOUTH) {
                bot.turnLeft();
            }
            // loop to move to target street
            while (bot.getStreet() != targetStreet) {
                bot.move();
            }
        }
        
        // check if bot avenue is greater than target avenue
        if (bot.getAvenue() > targetAvenue) {
            // loop to face west
            while (bot.getDirection() != Direction.WEST) {
                bot.turnLeft();
            }
            // loop to move to target avenue
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        } 
        // check if bot avenue is less than target avenue
        else if (bot.getAvenue() < targetAvenue) {
            // loop to face east
            while (bot.getDirection() != Direction.EAST) {
                bot.turnLeft();
            }
            // loop to move to target avenue
            while (bot.getAvenue() != targetAvenue) {
                bot.move();
            }
        }
    }
}