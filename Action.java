package g12CP_FinalProject;

public class Action {
	private int targetStreet;
	private int targetAvenue;
	private String actionPerformed;
	
	public Action(int targetStreet, int targetAvenue, String actionPerformed) {
		this.targetStreet = targetStreet;
		this.targetAvenue = targetAvenue;
		this.actionPerformed = actionPerformed;
	}

	public void setTargetStreet(int street) {
		this.targetStreet = street;
	}
	public void setTargetAvenue(int avenue) {
		this.targetStreet = avenue;
	}
	
	public void setActionPerformed(String action) {
		this.actionPerformed= action;
	}
	
	public int getTargetStreet() {
		return this.targetStreet;
	}
	
	public int getTargetAvenue() {
		return this.targetAvenue;
	}
	
	public String getActionPerformed() {
		return this.actionPerformed;
	}

}
