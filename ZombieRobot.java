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
     */
    public ZombieRobot(City c, int st, int ave, Direction dir, int id, int speed, int attackAbility) {
        super(c, st, ave, dir, id, speed, true);
        this.setColor(Color.GREEN);
        this.attackAbility = attackAbility;
        this.estimatedEvadeStats = new int[50]; 
        this.currentTargetId = -1;
        this.lastIntent = "";
        
        // initialize all estimates to a baseline quadrant 1 guess
        for(int i = 0; i < 50; i++) {
            this.estimatedEvadeStats[i] = 25;
        }
    }

    /**
     * gets the attack ability of the zombie for combat resolution
     * @return int attack ability
     */
    public int getAttackAbility() {
        return this.attackAbility;
    }

    /**
     * overriden method to take a turn
     * @param state robot info record game state
     * @return turnaction for the app engine
     */
    public TurnAction takeTurn(RobotInfoRecord[] state) {
        updateLearning(state);
        RobotInfoRecord[] targets = getValidTargets(state);
        insertionSortTargets(targets);
        
        TurnAction action = determineAction(targets);
        
        // remember what we tried to do so we can learn from it next turn
        this.lastIntent = action.getIntent();
        return action;
    }

    /**
     * counts valid survivors adds them to a array of records for targets
     * @param state robot info record game state
     * @return robot info record target list
     */
    private RobotInfoRecord[] getValidTargets(RobotInfoRecord[] state) {
        int survivorCount = 0;
        // loop through state array to count valid survivors
        for (int i = 0; i < state.length; i++) {
            // check if robot is not a zombie and not self
            if (state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
                survivorCount++;
            }
        }

        RobotInfoRecord[] targets = new RobotInfoRecord[survivorCount];
        int index = 0;
        // loop through state array to populate targets
        for(int i = 0; i < state.length; i++) {
            // check if robot is not a zombie and not self
            if(state[i] != null && !state[i].getIsZombie() && state[i].getId() != this.id) {
                targets[index] = state[i];
                index++;
            }
        }
        
        return targets;
    }

    /**
     * deciding on which action is appropriate for the current array of targets
     * @param targets robot info record targets 
     * @return turnaction deciding on the course of action
     */
    private TurnAction determineAction(RobotInfoRecord[] targets) {
        // check if there are any targets left
        if(targets.length > 0) {
            RobotInfoRecord bestTarget = targets[0];
            this.currentTargetId = bestTarget.getId();

            double distanceToBest = calculateDistance(bestTarget.getStreet(), bestTarget.getAvenue());

            // check if target is close enough to infect
            if(distanceToBest <= 1.0) {
                TurnAction action = new TurnAction(bestTarget.getStreet(), bestTarget.getAvenue(), TurnAction.INFECT);
                action.setTargetBot(this.currentTargetId);
                return action;
            }
            // check if target is far away
            else {
                int nextStreet = this.getStreet();
                int nextAvenue = this.getAvenue();

                // check if vertical distance is greater than horizontal
                if(Math.abs(bestTarget.getStreet() - this.getStreet()) > Math.abs(bestTarget.getAvenue() - this.getAvenue())) {
                    // check if target is south
                    if(bestTarget.getStreet() > this.getStreet()) {
                        nextStreet++;
                    } 
                    // check if target is north
                    else { 
                        nextStreet--;
                    }
                } 
                // check if horizontal distance is greater or equal
                else {
                    // check if target is east
                    if(bestTarget.getAvenue() > this.getAvenue()) {
                        nextAvenue++;
                    } 
                    // check if target is west
                    else {
                        nextAvenue--;
                    }
                }
                
                return new TurnAction(nextStreet, nextAvenue, TurnAction.MOVE);
            }
        }

        return new TurnAction(this.getStreet(), this.getAvenue(), "WAIT");
    }

    /**
     * sorts the targets by their calculated score
     * @param arr robot info record array of targets
     */
    private void insertionSortTargets(RobotInfoRecord[] arr) {
        // loop through array for insertion sort starting at second element
        for(int i = 1; i < arr.length; i++) {
            RobotInfoRecord key = arr[i];
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
     * @param target the robot to evaluate
     * @return double the calculated threat score
     */
    private double calculateTargetScore(RobotInfoRecord target) {
        double distance = calculateDistance(target.getStreet(), target.getAvenue());
        int itemsCarried = target.getItemsCarried();
        
        // fetch our current guess of their evade ability
        int estimatedEvade = estimatedEvadeStats[target.getId()];

        // high evade ability is bad so it adds heavily to the threat score
        return distance - (itemsCarried * 2.0) + (estimatedEvade / 10.0); 
    }

    /**
     * evaluates board to learn about survivor abilities based on interaction success
     * @param state the current board state
     */
    private void updateLearning(RobotInfoRecord[] state) {
        // check if we attacked someone last turn
        if(currentTargetId != -1 && lastIntent.equals(TurnAction.INFECT)) {
            boolean targetStillAlive = false;

            // loop through state array to find target
            for(int i = 0; i < state.length; i++) {
                // check if target is found and still a survivor
                if(state[i] != null && state[i].getId() == currentTargetId && !state[i].getIsZombie()) {
                    targetStillAlive = true;
                    break; 
                }
            }

            // check if target evaded infection and won the dice roll
            if(targetStillAlive) {
                // bump up our guess of their evade ability since they survived
                estimatedEvadeStats[currentTargetId] += 25;
                
                // cap the guess at 100 max
                if (estimatedEvadeStats[currentTargetId] > 100) {
                    estimatedEvadeStats[currentTargetId] = 100;
                }
            } 
            // check if target was successfully infected
            else {
                currentTargetId = -1;
            }
        }
    }

    @Override
    public RobotInfoRecord generateRecord() {
        return new RobotInfoRecord(this.getId(), this.getStreet(), this.getAvenue(), (int) this.getSpeed(), this.isZombie(), 0);
    }

    @Override
    public int getCombatAbility() {
        return this.attackAbility;
    }
}