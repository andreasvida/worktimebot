package clocking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface TimeClock {
	
	public void clockIn(Instant when);
	
	public void clockOut(Instant when);
		
	public Duration getWorkedTime();
		
	public Duration getWorkedToday();
	
	public Duration getBalance(Instant when);
	
	public String printList(Locale locale);
	
	public void setTimeZone(ZoneId z);
	
	public void setOffDays(Set<LocalDate> offDays);
	
	public void setHours(Duration[] week);
	
	public Set<LocalDate> getOffDays();
	
	public boolean isClockedIn();

	/**
	 * Convenience method
	 */
	default public void clock() {		
		if(isClockedIn()) clockOut(Instant.now()); else clockIn(Instant.now());		
	}

	public void addComment(Instant when, String command);

}
