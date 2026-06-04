package final_project_2026;

import java.awt.*;
import becker.robots.*;

public class SurvivorRobot extends GameRobot {

    private int dodgeAbility;
    private int baseSpeed;
    private int maxCapacity;
    private int currentItems;
    private boolean isZombie;

    public SurvivorRobot(City c, int st, int ave, Direction dir, int id, int speed, int dodgeAbility, int maxCapacity) {
        super(c, st, ave, dir, id, speed, false);
        this.dodgeAbility = dodgeAbility;
        this.baseSpeed = speed;
        this.maxCapacity = maxCapacity;
        this.currentItems = 0;
        this.setColor(Color.orange);
    }

    @Override
    public TurnAction takeTurn(RobotInfoRecord[] state) {
        // return actual forage logic instead of hardcoded move
        return forageMode();
    }

    public void executeApprovedMove(int spaces) {
        // loop for number of spaces
        for (int i = 0; i < spaces; i++) {
            this.move();
        }
    }

    private TurnAction forageMode() {
        // check if survivor is on top of a thing
        if (this.canPickThing()) {
            return new TurnAction(this.getStreet(), this.getAvenue(), TurnAction.PICK_UP);
        }

        int plannedStreet = this.getStreet();
        int plannedAvenue = this.getAvenue();

        // check if robot is facing east
        if (this.getDirection() == Direction.EAST) {
            // check if robot is not at the end of avenue
            if (this.getAvenue() < 24) {
                plannedAvenue++;
            }
            // check if robot is at end of avenue
            else {
                plannedStreet++;
            }
        }
        // check if robot is facing west
        else if (this.getDirection() == Direction.WEST) {
            // check if robot is not at the start of avenue
            if (this.getAvenue() > 1) {
                plannedAvenue--;
            }
            // check if robot is at start of avenue
            else {
                plannedStreet++;
            }
        }
        // check if robot is facing north or south
        else {
            plannedAvenue++;
        }

        return new TurnAction(plannedStreet, plannedAvenue, TurnAction.MOVE);
    }

    private void sortThreats(RobotInfoRecord[] state) {
        int n = state.length;

        // loop through entire array
        for (int i = 0; i < n - 1; i++) {
            int mostDangerousIndex = i;

            // loop through unsorted portion of array
            for (int j = i + 1; j < n; j++) {
                // check if the record is not null and the robot being looked at is a zombie
                if (state[j] != null && state[j].getIsZombie()) {
                    double distanceJ = calculateDistance(state[j].getStreet(), state[j].getAvenue());

                    double currentMinDistance;
                    // check if the current most dangerous is a zombie
                    if (state[mostDangerousIndex] != null && state[mostDangerousIndex].getIsZombie()) {
                        currentMinDistance = calculateDistance(state[mostDangerousIndex].getStreet(),
                                state[mostDangerousIndex].getAvenue());
                    }
                    // check if current most dangerous is not a zombie
                    else {
                        currentMinDistance = Double.MAX_VALUE;
                    }

                    // check if the new zombie is closer than current closest
                    if (distanceJ < currentMinDistance) {
                        mostDangerousIndex = j;
                    }
                    // check if two zombies are the same distance away
                    else if (distanceJ == currentMinDistance) {
                        // check if the new zombie is faster
                        if (state[j].getSpeed() > state[mostDangerousIndex].getSpeed()) {
                            mostDangerousIndex = j;
                        }
                    }
                }
            }

            // check if a more dangerous zombie was found
            if (mostDangerousIndex != i) {
                RobotInfoRecord temp = state[mostDangerousIndex];
                state[mostDangerousIndex] = state[i];
                state[i] = temp;
            }
        }
    }

    @Override
    public RobotInfoRecord generateRecord() {
        return new RobotInfoRecord(this.getId(), this.getStreet(), this.getAvenue(), this.speed, this.isZombie(),
                this.currentItems);
    }

    @Override
    public int getCombatAbility() {
        return this.dodgeAbility;
    }

	@Override
	public String getRole() {
		// TODO Auto-generated method stub
		return null;
	}
}
