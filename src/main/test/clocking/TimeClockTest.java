package clocking;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.clocking.TimeClock;
import java.clocking.TimeClockImpl;
import java.time.Duration;
import org.junit.Test;


public class TimeClockTest {	
	
	@Test
	public void simpleInOut() {
		TimeClock clock = new TimeClockImpl();		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(10), d);				
	}

}
