package clocking.impl;

import java.io.Serializable;

public class ClockEntry implements Serializable {
			
	private static final long serialVersionUID = 1L;
	
	public Boolean isClockIn;
	public String comment;
	
	public ClockEntry(boolean isClockIn) { 
		this.isClockIn = isClockIn;
	}
	
	public ClockEntry(String comment) {
		this.comment = comment;
	}
	
	public boolean isComment() { 
		return comment != null;
	}
	
	public boolean isClockIn() { 
		return Boolean.TRUE.equals(isClockIn);
	}
	
	public String getComment() {
		return comment;
	}
	

}
