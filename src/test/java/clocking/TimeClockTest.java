package clocking;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import clocking.TimeClock;
import clocking.impl.TimeClockImpl;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;


public class TimeClockTest {
	
	TimeClock clock;
	
	@Before
	public void init() { 
		clock =  new TimeClockImpl();
	}
	
	@Test
	public void simpleInOut() {			
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(10), d);				
	}
	
	@Test
	public void onlyIn() {			
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));				
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(0), d);				
	}
	
	@Test
	public void onlyOut() {			
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));					
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(0), d);				
	}
	
	
	@Test
	public void stillClockedIn() {				
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(10), d);				
	}
	
	@Test
	public void reversedOrder() {
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		Duration d = clock.getWorkedTime();		
		assertEquals(Duration.ofMinutes(20), d);				
	}
	
	@Test
	public void simpleBalance() {				
		// worked 20
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		
		// needed 10
		clock.setHours(new Duration[] {Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10),
				Duration.ofMinutes(10)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 1, 11, 11).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(10), balance);				
	}
	
	@Test
	public void balanceTwoDays() {

		clock.clockIn(LocalDateTime.of(2020, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2020, 1, 1, 10, 10).toInstant(ZoneOffset.ofHours(0)));		
		clock.clockIn(LocalDateTime.of(2020, 1, 2, 11, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2020, 1, 2, 11, 10).toInstant(ZoneOffset.ofHours(0)));
		
		clock.setHours(new Duration[] {
				Duration.ofMinutes(0),
				Duration.ofMinutes(0),
				Duration.ofMinutes(20), // Wed Jan 1, 2020: clocked 10
				Duration.ofMinutes(0),  // Thu Jan 2, 2020: clocked 10
				Duration.ofMinutes(20),  
				Duration.ofMinutes(0),
				Duration.ofMinutes(0)});
		
		Duration balance = clock.getBalance(LocalDateTime.of(2020, 1, 2, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(0), balance);
		
		// after second clock interval
		balance = clock.getBalance(LocalDateTime.of(2020, 1, 3, 10, 0).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(-20), balance);	
	}
	
	@Test
	public void balanceThreeDays() {		
		
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
				Duration.ofMinutes(0),  // Jan 1, 2020 = Saturday
				Duration.ofMinutes(0)});		
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 3, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(20), balance);				
	}
	
	@Test
	public void balanceWithoutClockIn() {		
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
	
	@Test
	public void considerHoliday() {
		clock.setHours(new Duration[] {Duration.ofMinutes(0),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20),
				Duration.ofMinutes(20)});
		clock.getOffDays().add(LocalDate.of(2000, 1, 3)); // January 3rd, 2000 = Monday => holiday
		
		// clocked = 20 
		clock.clockIn(LocalDateTime.of(2000, 1, 1, 10, 0).toInstant(ZoneOffset.ofHours(0)));
		clock.clockOut(LocalDateTime.of(2000, 1, 1, 10, 20).toInstant(ZoneOffset.ofHours(0)));
		
		// balance Thursday should be 0
		Duration balance = clock.getBalance(LocalDateTime.of(2000, 1, 2, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(-20), balance);
		
		// balance Friday should be 
		balance = clock.getBalance(LocalDateTime.of(2000, 1, 3, 11, 10).toInstant(ZoneOffset.ofHours(0)));		
		assertEquals(Duration.ofMinutes(-20), balance);		
	}
}
