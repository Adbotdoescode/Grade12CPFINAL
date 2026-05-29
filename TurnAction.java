package g12CP_FinalProject;

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
    private int targetBot;

    public TurnAction(int targetStreet, int targetAvenue, String intent) {
        this.targetStreet = targetStreet;
        this.targetAvenue = targetAvenue;
        this.intent = intent;
    }
    
    public void setTargetBot(int id) {
    	this.targetBot = id;
    }

    public int getTargetStreet() { 
        return this.targetStreet; 
    }

    public int getTargetBot() { 
    	return this.targetBot; 
    }
    
    public int getTargetAvenue() { 
        return this.targetAvenue; 
    }
    
    public String getIntent() {
        return this.intent; 
    }

}
