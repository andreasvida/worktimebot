package worktimebot;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;

public class UserSettings implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Duration[] hours;	
	public Locale locale; 	
	public ZoneId zoneId;
	
	public UserSettings(Duration[] hours, Locale locale, ZoneId zoneId) {
		super();
		this.hours = hours;
		this.locale = locale;
		this.zoneId = zoneId;
	}

	public Duration[] getHours() {
		return hours;
	}

	public void setHours(Duration[] hours) {
		this.hours = hours;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}
		

}
