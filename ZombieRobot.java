package final_project_2026;

import java.awt.Color;
import becker.robots.*;

public class ZombieRobot extends GameRobot {

    private int[] estimatedEvadeStats;
    private int currentTargetId;
    private String lastIntent;
    private int attackAbility;

    /**
     * constructor to create the zombie robot 
     * @param c city robot starts in 
     * @param st int street robot starts in
     * @param ave int avenue robot starts in 
     * @param dir direction robot faces in initially 
     * @param id int robot id
     * @param speed int speed of the robot
     * @param attackAbility int combat strength from 1 to 100
     * @param totalPlayers int the maximum amount of players in the game (for memory sizing)
     */
    public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed, int attackAbility, int totalPlayers) {
        super(c, st, ave, dir, id, speed, true);
        this.setColor(Color.GREEN);
        this.attackAbility = attackAbility;
        this.currentTargetId = -1;
        this.lastIntent = "";
        
        // Array scales perfectly with the game size to prevent out-of-bounds crashes
        this.estimatedEvadeStats = new int[totalPlayers]; 
        
        // initialize all estimates to a baseline quadrant 1 guess
        for(int i = 0; i < totalPlayers; i++) {
            this.estimatedEvadeStats[i] = 25;
        }
    }

    /**
     * overriden method to take a turn
     * @param state robot info record game state
     * @return turnaction for the app engine
     */
    public TurnAction takeTurn(RobotInfoRecord[] state) {
        updateLearning(state);
        
        // We now generate our custom Level 4 target list!
        SurvivorInfoRecord[] targets = getValidTargets(state);
        insertionSortTargets(targets);
        
        TurnAction action = determineAction(targets);
        
        // remember what it tried to do so it can learn from it next turn
        this.lastIntent = action.getIntent();
        return action;
    }

    /**
     * counts valid survivors and packs them into custom SurvivorInfoRecords
     * @param state robot info record game state
     * @return survivor info record target list
     */
    private SurvivorInfoRecord[] getValidTargets(RobotInfoRecord[] state) {
        int survivorCount = 0;
        // loop through state array to count valid survivors
        for (int i = 0; i < state.length; i++) {
            if (state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
                survivorCount++;
            }
        }

        SurvivorInfoRecord[] targets = new SurvivorInfoRecord[survivorCount];
        int index = 0;
        
        // loop through state array to populate targets
        for(int i = 0; i < state.length; i++) {
            if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
                
                // get learned memory of this specific survivor
                int knownEvadeStat = this.estimatedEvadeStats[state[i].getId()];
                
                // put the generic engine data and custom memory into the survivor record
                targets[index] = new SurvivorInfoRecord(
                    state[i].getId(), 
                    state[i].getStreet(), 
                    state[i].getAvenue(), 
                    state[i].getSpeed(), 
                    state[i].getIsZombie(), 
                    state[i].getItemsCarried(),
                    knownEvadeStat
                );
                
                index++;
            }
        }
        
        return targets;
    }

    /**
     * deciding on which action is appropriate for the current array of targets
     * @param targets survivor info record targets 
     * @return turnaction deciding on the course of action
     */
    private TurnAction determineAction(SurvivorInfoRecord[] targets) {
        // check if there are any targets left
        if(targets.length > 0) {
            SurvivorInfoRecord bestTarget = targets[0];
            this.currentTargetId = bestTarget.getId();

            // using Manhattan distance to match how the engine processes distance (grid instead of straight line euclidean)
            int distanceToBest = calculateManhattanDistance(this.getStreet(), this.getAvenue(), bestTarget.getStreet(), bestTarget.getAvenue());

            // if the target is within the zombie's speed limit
            if(distanceToBest <= this.getSpeed()) {
                TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
                action.setTargetBot(this.currentTargetId);
                return action;
            }
            // If target is out of range, sprint towards them using all available speed
            else {
                int nextStreet = this.getStreet();
                int nextAvenue = this.getAvenue();
                int speedLeft = (int) this.getSpeed();

                // Loop to consume all movement points to get as close as possible
                while (speedLeft > 0 && calculateManhattanDistance(nextStreet, nextAvenue, bestTarget.getStreet(), bestTarget.getAvenue()) > 0) {
                    if(Math.abs(bestTarget.getStreet() - nextStreet) > Math.abs(bestTarget.getAvenue() - nextAvenue)) {
                        if(bestTarget.getStreet() > nextStreet) { 
                            nextStreet++; 
                        } 
                        else { 
                            nextStreet--; 
                        }
                    } 
                    else {
                        if(bestTarget.getAvenue() > nextAvenue) { 
                            nextAvenue++; 
                        } 
                        else { 
                            nextAvenue--; 
                        }
                    }
                    speedLeft--;
                }
                
                return new TurnAction(nextStreet, nextAvenue, TurnAction.MOVE);
            }
        }

        return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
    }

    /**
     * sorts the targets by their calculated score
     * @param arr survivor info record array of targets
     */
    private void insertionSortTargets(SurvivorInfoRecord[] arr) {
        // loop through array for insertion sort starting at second element
        for(int i = 1; i < arr.length; i++) {
            SurvivorInfoRecord key = arr[i];
            double keyScore = calculateTargetScore(key);
            int j = i - 1;

            // loop while element has higher score than key
            while (j >= 0 && calculateTargetScore(arr[j]) > keyScore) {
                arr[j + 1] = arr[j];
                j = j - 1; 
            }
            arr[j+1] = key;
        }
    }

    /**
     * calculating the score of the target which will be sorted by
     * @param target the survivor to evaluate
     * @return double the calculated threat score
     */
    private double calculateTargetScore(SurvivorInfoRecord target) {
        int distance = calculateManhattanDistance(this.getStreet(), this.getAvenue(), target.getStreet(), target.getAvenue());
        int itemsCarried = target.getItemsCarried();
        
        int estimatedEvade = target.getEstimatedEvade();

        // high evade ability is bad so it adds heavily to the threat score
        return distance - (itemsCarried * 2.0) + (estimatedEvade / 10.0); 
    }

    /**
     * evaluates board to learn about survivor abilities based on interaction success
     * @param state the current board state
     */
    private void updateLearning(RobotInfoRecord[] state) {
        // check if it attacked someone last turn
        if(currentTargetId != -1 && lastIntent.equals(TurnAction.INFECT)) {
            boolean targetStillAlive = false;

            // loop through state array to find target
            for(int i = 0; i < state.length; i++) {
                if(state[i] != null && state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
                    targetStillAlive = true;
                    break; 
                }
            }

            // check if target evaded infection and won the dice roll
            if(targetStillAlive) {
                // bump up the guess of their evade ability since they survived
                estimatedEvadeStats[currentTargetId] += 25;
                
                // cap the guess at 100 max
                if (estimatedEvadeStats[currentTargetId] > 100) {
                    estimatedEvadeStats[currentTargetId] = 100;
                }
            } 
            else {
                currentTargetId = -1;
            }
        }
    }
    
    /**
     * helper to calculate grid distance matching the OutbreakApp engine
     */
    private int calculateManhattanDistance(int st1, int ave1, int st2, int ave2) {
        return Math.abs(st1 - st2) + Math.abs(ave1 - ave2);
    }

    @Override
    public int getCombatAbility() {
        return this.attackAbility;
    }
    
    @Override
    public String getRole() {
        return "ZOMBIE";
    }
}
