package final_project_2026;

public class ZombieInfoRecord extends RobotInfoRecord {
    private double distanceToMedic;
    private double totalPreferability = 0;
    private int dodges = 0;
    private int totalAttacks = 0;
    private double dodgeAbility;
    
    public ZombieInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, double distanceToMedic) {
        super(id, street, avenue, speed, isZombie, 0); 
        this.distanceToMedic = distanceToMedic;
//        this.totalPreferability = totalPreferability;
//        this.dodgeAbility = dodgeAbility;
    }
    
    public double getDistanceToMedic() {
        return this.distanceToMedic;
    }
    
    public void setTotalPreferability(double preferability) {
		this.totalPreferability = preferability;
	}
	
	public double getTotalPreferability() { 
		return this.totalPreferability;
	}
	
	public void increaseDodges () {
		this.dodges += 1;
	}
	
	public int getTotalAttacks () {
		return this.totalAttacks;
	}
	
	public void increaseTotalAttacks () {
		this.totalAttacks += 1;
	}
	
	public int getDodges () {
		return this.dodges;
	}
	
	public double getDodgeAbility() {
		return this.dodgeAbility;
	}
	
	public void setDodgeAbility(double dodgeAbility) { 
		this.dodgeAbility = dodgeAbility;
	}
}
