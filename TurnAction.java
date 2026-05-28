package final_project_2026;

/**
 * Permission slip to return to main to decide on movement.
 * @author Ayyan
 * @version May 25, 2026
 */
public class TurnAction {
    
    // Constants to prevent typos
    public static final String MOVE = "MOVE";
    public static final String PICK_UP = "PICK_UP";
    public static final String DROP_OFF = "DROP_OFF";
    public static final String INFECT = "INFECT";
    public static final String HEAL = "HEAL";

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
