package reservation.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.exceptions.ReservationAlreadyExistsException;
import org.coffeehouse.home.reservation.job.QueryAvailabilityJob.QueryAvailabilityJobFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReservationApplication.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class TestQueryAvailabilityJob {

	@Autowired
	private QueryAvailabilityJobFactory mQueryAvailabilityJobFactory;

	@Autowired
	private ReservationDao mReservationDao;

	@Test
	public void TestQueryAvailabilityJobNoAvailabilityOneReservation() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(2);
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);

		mReservationDao.save(reservation);

		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();

		assertTrue(availability.isEmpty());
	}

	@Test
	public void TestQueryAvailabilityJobNoAvailabilityTwoReservations() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(2);
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);

		LocalDate startDate2 = endDate.plusDays(1);
		LocalDate endDate2 = startDate2.plusDays(2);
		Reservation reservation2 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate2, endDate2);

		mReservationDao.save(reservation);
		mReservationDao.save(reservation2);

		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate2).call();

		assertTrue(availability.isEmpty());
	}

	@Test
	public void TestQueryAvailabilityJobAllAvailable() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		
		//Reservation that falls outside of the query range should not show up
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate.plusMonths(1), endDate.plusMonths(1));
		mReservationDao.save(reservation);

		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();

		assertEquals(3, availability.size());
		assertEquals(startDate, availability.get(0));
		assertEquals(startDate.plusDays(1), availability.get(1));
		assertEquals(endDate, availability.get(2));
	}
	
	@Test
	public void TestQueryAvailabilityJobWithReservationStraddling() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(5);
		
		// Reservation that Straddles the query period should return the dates that are still available.
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate.minusDays(1), startDate.plusDays(2));
		mReservationDao.save(reservation);
		
		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		
		assertEquals(3, availability.size());
		assertEquals(endDate.minusDays(2), availability.get(0));
		assertEquals(endDate.minusDays(1), availability.get(1));
		assertEquals(endDate, availability.get(2));
	}
	
	@Test
	public void TestQueryAvailabilityJobWithReservationInMiddle() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(6);
		
		// Reservation that Straddles the query period should return the dates that are still available.
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate.plusDays(1), endDate.minusDays(1));
		mReservationDao.save(reservation);
		
		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		
		assertEquals(2, availability.size());
		assertEquals(startDate, availability.get(0));
		assertEquals(endDate, availability.get(1));
	}
	
	/*
	 * Dates
	 * 1	free
	 * 2	reservation
	 * 3	reservation
	 * 4	reservation
	 * 5	free
	 * 6	free
	 * 7	reservation2
	 * 8	reservation2
	 * 9	reservation2
	 * 10	free
	 */
	@Test
	public void TestQueryAvailabilityJobWithMultipleReservationInMiddle() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = startDate.plusDays(10);
		
		// Reservation that Straddles the query period should return the dates that are still available.
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate.plusDays(1), startDate.plusDays(3));
		Reservation reservation2 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate.plusDays(6), startDate.plusDays(9));
		mReservationDao.save(reservation);
		mReservationDao.save(reservation2);
		
		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		
		assertEquals(4, availability.size());
		assertEquals(startDate, availability.get(0));
		assertEquals(startDate.plusDays(4), availability.get(1));
		assertEquals(startDate.plusDays(5), availability.get(2));
		assertEquals(endDate, availability.get(3));
	}

}
