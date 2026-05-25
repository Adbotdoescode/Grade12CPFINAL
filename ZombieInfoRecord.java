package g12CP_FinalProject;

public class ZombieInfoRecord extends RobotInfoRecord {
	private double distanceToMedic;
	
	public ZombieInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, double distanceToMedic) {
		super(id, street, avenue, speed, isZombie);
		this.distanceToMedic = distanceToMedic;
	}
	
	public double getDistanceToMedic() {
		return this.distanceToMedic;
	}
}
