package clocking;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import clocking.TimeClock;
import clocking.impl.TimeClockImpl;
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
	
	@Test
	public void onlyIn() {
		TimeClock clock = new TimeClockImpl();		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));				
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(0), d);				
	}
	
	@Test
	public void onlyOut() {
		TimeClock clock = new TimeClockImpl();		
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));					
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(0), d);				
	}
	
	
	@Test
	public void stillClockedIn() {
		TimeClock clock = new TimeClockImpl();			
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(10), d);				
	}
	
	@Test
	public void reversedOrder() {
		TimeClock clock = new TimeClockImpl();			
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(20), d);				
	}
	
	@Test
	public void simpleBalance() {
		TimeClock clock = new TimeClockImpl();			
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		
		clock.setHours(new Duration[] {Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 1, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(20), balance);				
	}
	
	@Test
	public void balanceTwoDays() {
		TimeClock clock = new TimeClockImpl();			
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 2, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 2, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		
		clock.setHours(new Duration[] {Duration.ofMinutes(0),
				Duration.ofMinutes(0),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(0),
				Duration.ofMinutes(0),
				Duration.ofMinutes(0)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 2, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(-20), balance);				
	}
	
	@Test
	public void balanceThreeDays() {
		TimeClock clock = new TimeClockImpl();
		
		// worked = 20 minutes.
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 2, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 2, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		
		clock.setHours(new Duration[] {Duration.ofMinutes(0),
				Duration.ofMinutes(0),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(60),
				Duration.ofMinutes(0),
				Duration.ofMinutes(0)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 3, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(-80), balance);				
	}
	
	@Test
	public void balanceWithoutClockIn() {
		TimeClock clock = new TimeClockImpl();
		
		// worked = 0 minutes.
		
		clock.setHours(new Duration[] {Duration.ofMinutes(0),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 3, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(0), balance);				
	}
	
	

}
