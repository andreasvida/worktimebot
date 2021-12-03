package clocking.impl;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import clocking.TimeClock;

public class TimeClockImpl implements TimeClock, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Set<LocalDate> offDays;
	
	private TreeMap<Instant, ClockEntry> map;
	
	private Duration[] hours; 
	
	private ZoneId zoneid;

	public TimeClockImpl() {
		map = new TreeMap<Instant, ClockEntry>();
		zoneid = ZoneOffset.systemDefault();
	}

	@Override
	public void clockIn(Instant when) {
		map.put(when, new ClockEntry(true));

	}

	@Override
	public void clockOut(Instant when) {
		map.put(when, new ClockEntry(false));

	}

	@Override
	public void setTimeZone(ZoneId z) { 
		zoneid = z;
	}

	private Duration getWorked(boolean today) {
		Instant start = null;
		Duration result = Duration.ZERO;
		Instant now = Instant.now();

		for (Entry<Instant, ClockEntry> entry : map.entrySet()) {
			if(entry.getValue().isComment()) continue;
			
			if ((today && now.truncatedTo(ChronoUnit.DAYS).equals(entry.getKey().truncatedTo(ChronoUnit.DAYS))) || !today) {

				if (start == null) {
					if (entry.getValue().isClockIn()) {
						start = entry.getKey();
					} // else : clockOut without clockIn

				} else {
					if (!entry.getValue().isClockIn()) {
						Instant end = entry.getKey();
						result = result.plus(Duration.between(start, end));
						start = null;
					} // else : clockIn without clockOut

				}

			}
		}
		return result;
	}

	@Override
	public Duration getWorkedToday() {
		return getWorked(true);
	}

	@Override
	public Duration getWorkedTime() {
		return getWorked(false);
	}

	@Override
	public Duration getBalance(Instant when) {		
		if(isClockedIn()) {
			
			//simulate clock out and c
			TimeClockImpl impl = new TimeClockImpl();
			impl.map.putAll(map);
			impl.map.put(when, new ClockEntry(false));
			return impl.getBalance(when);
		}
		
		Duration total = getWorkedTime();
		
		Instant startDay = null;		
		for(Entry<Instant, ClockEntry> entry: map.entrySet()) {
			if(entry.getValue().isClockIn()) { 
				startDay = entry.getKey(); 
				break;
			}
		}
		
		if (startDay == null) return Duration.ZERO;
		
		Duration needed = Duration.of(0, ChronoUnit.SECONDS);
		
		LocalDate startLocalDay = startDay.atZone(zoneid).toLocalDate();
		LocalDate endLocalDay = when.atZone(zoneid).toLocalDate();
		
		while (!startLocalDay.isAfter(endLocalDay)) {			
			needed = needed.plus(needed(startLocalDay));
			startLocalDay = startLocalDay.plusDays(1);
		}
		return total.minus(needed);		
	}

	private Duration needed(LocalDate d) {		
		if(getOffDays().contains(d)) {
			return Duration.ZERO;
		}
		
		if(hours != null) { 
			return hours[d.getDayOfWeek().getValue()-1];
		} else { 
			return Duration.ZERO;
		}	
	}

	@Override
	public String printList(Locale locale) {		
		StringBuilder result = new StringBuilder();		
		LocalDate currentDay = null;
		
		StringBuilder comments = new StringBuilder();
		
		DateTimeFormatter dateF = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
		DateTimeFormatter timeF = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale);
		
		for(Entry<Instant, ClockEntry> entry: map.entrySet()) {
			if(entry.getValue().isComment() && entry.getValue().getComment() != null) {				
				String comment = entry.getValue().getComment().trim();				
				if(!comment.isEmpty()) {
					String dateStr = dateF.format(entry.getKey().atZone(zoneid).toLocalDate());								
					comments.append(dateStr).append(":\n").append(comment).append("\n");
				}
				continue;
			}
			Instant i = entry.getKey();			
			LocalDate d = i.atZone(zoneid).toLocalDate();						
			
			if(!d.equals(currentDay)) {
				currentDay = d; 				
				result.append("\n" + dateF.format(currentDay) + ": "); 				
			}
			
			if(entry.getValue().isClockIn()) result.append(" "); else result.append("-");
			result.append(timeF.format(entry.getKey().atZone(zoneid).toLocalTime()));
									
		}		
		return result.append("\n").append(comments).toString();		
	}

	@Override
	public boolean isClockedIn() {
		int i = 0;
		for(ClockEntry e: map.values()) {
			if(!e.isComment()) {			
				if(e.isClockIn()) i++; else i--;
			}
		}
		return (i % 2) > 0;
	}

	@Override
	public void setOffDays(Set<LocalDate> offDays) {
		this.offDays = offDays;		
		if(offDays == null) offDays = new HashSet<>();
	}
	
	@Override
	public Set<LocalDate> getOffDays() { 
		return offDays == null ? offDays = new HashSet<>() : offDays; 
	}

	@Override
	public void addComment(Instant when, String comment) {
		map.put(when, new ClockEntry(comment));		
	}

	@Override
	public void setHours(Duration[] hours) {
		if(hours != null && hours.length == 7) { 
			this.hours = hours;
		}		
	}

}
