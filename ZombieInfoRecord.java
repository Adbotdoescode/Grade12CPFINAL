package final_project_2026;

public class ZombieInfoRecord extends RobotInfoRecord {
    private double distanceToMedic;
    private double undesirability = 0;
    private int dodges = 0;
    private int totalAttacks = 0;
    private double dodgeAbility = 0;
    
    public ZombieInfoRecord(int id, int street, int avenue, int speed, boolean isZombie, double distanceToMedic) {
        super(id, street, avenue, speed, isZombie, 0); 
        this.distanceToMedic = distanceToMedic;
    }
    
    public double getDistanceToMedic() {
        return this.distanceToMedic;
    }
    
    public void setTotalUndesirability(double preferability) {
		this.undesirability = preferability;
	}
    
	
	public double getTotalUndesirability() { 
		return this.undesirability;
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
	
	public int getTotalDodges () {
		return this.dodges;
	}
	
	public double getDodgeAbility() {
		return this.dodgeAbility;
	}
	
	public void setDodgeAbility(double dodgeAbility) { 
		this.dodgeAbility = dodgeAbility;
	}
	
	public void setTotalDodges(int dodges) {
		this.dodges = dodges;
	}
	
	public void setTotalAttacks(int totalAttacks) {
		this.totalAttacks = totalAttacks;
	}
}
