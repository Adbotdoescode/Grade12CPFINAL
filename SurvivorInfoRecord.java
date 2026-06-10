package final_project_2026;

public class SurvivorInfoRecord extends RobotInfoRecord {
	
	private int estimatedEvade;

	public SurvivorInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, int itemsCarried, int estimatedEvade) {
		super(id, street, avenue, speed, isZombie, itemsCarried);
		this.estimatedEvade = estimatedEvade;
	}

	public int getEstimatedEvade() {
		return this.estimatedEvade;
	}
}