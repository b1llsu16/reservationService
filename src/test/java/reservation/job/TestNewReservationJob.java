package reservation.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.exceptions.DateRangeException;
import org.coffeehouse.home.reservation.job.NewReservationJob.NewReservationJobFactory;
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
public class TestNewReservationJob {

	@Autowired
	private NewReservationJobFactory mReservationJobFactory;
	
	@Autowired
	private ReservationDao mReservationDao;
	
	/*
	 * C2. The campsite can be reserved for max 3 days
	 */
	@Test( expected = DateRangeException.class )
	public void testReservationExceedMaxAllowedLength() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(10);
		
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		
		mReservationJobFactory.getJob(reservation).call();
	}
	
	/*
	 * C3. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. 
	 */
	@Test( expected = DateRangeException.class )
	public void testReservationInvalidStartDate() throws Exception {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(2);
		
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		
		mReservationJobFactory.getJob(reservation).call();
	}
	
	/*
	 * C3. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. 
	 * We do not allow reservations that include days that are more than a month in advance.
	 */
	@Test( expected = DateRangeException.class )
	public void testReservationInvalidEndDate() throws Exception {
		LocalDate startDate = LocalDate.now().plusMonths(1);
		LocalDate endDate = LocalDate.now().plusMonths(1).plusDays(2);
		
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		
		mReservationJobFactory.getJob(reservation).call();
	}
	
	@Test( expected = DateRangeException.class )
	public void testReservationAlreadyReserved() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = startDate.plusDays(2);
		
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		
		mReservationJobFactory.getJob(reservation).call();
		
		LocalDate startDate2 = LocalDate.now().plusDays(3);
		LocalDate endDate2 = startDate.plusDays(2);
		
		//Failed reservation, date(s) already taken
		Reservation reservation2 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate2, endDate2);
		
		mReservationJobFactory.getJob(reservation2).call();
	}
	
	@Test
	public void testReservationJob() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		
		mReservationJobFactory.getJob(reservation).call();
		
		Optional<Reservation> found = mReservationDao.findById(reservation.getId());
		assertTrue(found.isPresent());
		assertEquals(reservation.getName(), found.get().getName());
		assertEquals(reservation.getEmail(), found.get().getEmail());
		assertEquals(reservation.getId(), found.get().getId());
		assertEquals(startDate, found.get().getStartDate());
		assertEquals(endDate, found.get().getEndDate());
	}
}
