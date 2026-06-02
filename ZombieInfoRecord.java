package final_project_2026;

public class ZombieInfoRecord extends RobotInfoRecord {
    private double distanceToMedic;
    
    public ZombieInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, double distanceToMedic) {
        super(id, street, avenue, speed, isZombie, 0); 
        this.distanceToMedic = distanceToMedic;
    }
    
    public double getDistanceToMedic() {
        return this.distanceToMedic;
    }
}