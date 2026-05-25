package final_project_2026;


/**
 * Permission slip to retunr to main to decide on movemnt
 * @author Ayan
 * @version May 25, 2026
 */
public class TurnAction {
    private int targetStreet;
    private int targetAvenue;
    private String intent; 

    public TurnAction(int targetStreet, int targetAvenue, String intent) {
        this.targetStreet = targetStreet;
        this.targetAvenue = targetAvenue;
        this.intent = intent;
    }

    public int getTargetStreet() { 
		return targetStreet; 
	}
    public int getTargetAvenue() { 
		return targetAvenue; 
	}
    public String getIntent() {
		return intent; 
	}
}
