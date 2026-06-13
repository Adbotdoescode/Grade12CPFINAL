package final_project_2026;


/**
 * The TurnAction is a class used to define a record that is used to help the robots communciate with the applciation class. Each robot returns a TurnAction record in its takeTurn method containing what it wants to do. The application class checks weather the robots move is legal and executes moves based on this record. 
 * @author ayyan
 * @version June 12, 2026
 * 
 */
public class TurnAction {
	
	// Setup instance variables 
    // constants to prevent typos
    public static final String MOVE = "MOVE";
    public static final String PICK_UP = "PICK_UP";
    public static final String DROP_OFF = "DROP_OFF";
    public static final String INFECT = "INFECT";
    public static final String HEAL = "HEAL";
    public static final String HEAL_SELF = "HEAL_SELF";

    private int targetStreet;
    private int targetAvenue;
    private String intent; 
    private int targetBot;

    /**
     * The constructor for the turnAction record, used to initialize a turnAction record
     * @param targetStreet
     * @param targetAvenue
     * @param intent
     */
    public TurnAction(int targetStreet, int targetAvenue, String intent) {
        this.targetStreet = targetStreet;
        this.targetAvenue = targetAvenue;
        this.intent = intent;
    }
    
    /**
     * Setter method for setting the target ID the action is intended to be performed on 
     * @param id
     */
    public void setTargetBot(int id) {
        this.targetBot = id;
    }

    
    /**
     * getter method for retrieving the attribute targetStreet
     * @return - returns the int value for the attribute targetStreet
     */
    public int getTargetStreet() { 
        return this.targetStreet; 
    }

    
    /**
     * getter method for attribute targetBot
     * @return - returns the int value for targetBot
     */
    public int getTargetBot() { 
        return this.targetBot; 
    }
    
    /** 
     * getter method for the attribute targetAvenue
     * @return - returns the int value for targetAvenue
     */
    public int getTargetAvenue() { 
        return this.targetAvenue; 
    }
    
    /**
     * getter method for the attribute intent
     * @return - returns the String value containing the intent
     */
    public String getIntent() {
        return this.intent; 
    }
}
