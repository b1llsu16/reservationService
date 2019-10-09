package reservation.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.exceptions.DateRangeException;
import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
import org.coffeehouse.home.reservation.job.UpdateReservationJob.UpdateReservationJobFactory;
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
public class TestUpdateReservationJob {

	@Autowired
	private UpdateReservationJobFactory mUpdateReservationJobFactory;

	@Autowired
	private ReservationDao mReservationDao;

	/*
	 * C2. The campsite can be reserved for max 3 days
	 */
	@Test(expected = DateRangeException.class)
	public void testModificationExceedMaxAllowedLength() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(4);

		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationDao.save(reservation);

		Optional<Reservation> original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isPresent());

		// Extend Reservation
		LocalDate newStartDate = LocalDate.now().plusDays(2);
		LocalDate ewEdDate = LocalDate.now().plusDays(10);
		Reservation modification = new Reservation("Brian Leung", "brian.leung@mail.com", newStartDate, ewEdDate);

		mUpdateReservationJobFactory.getJob(reservation.getId(), modification).call();
	}

	/*
	 * C3. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to
	 * 1 month in advance.
	 */
	@Test(expected = DateRangeException.class)
	public void testModificationInvalidStartDate() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(4);

		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationDao.save(reservation);

		Optional<Reservation> original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isPresent());

		// Move Reservation to today
		LocalDate newStartDate = LocalDate.now();
		LocalDate newEndDate = LocalDate.now().plusDays(2);
		Reservation modification = new Reservation("Brian Leung", "brian.leung@mail.com", newStartDate, newEndDate);

		mUpdateReservationJobFactory.getJob(reservation.getId(), modification).call();
	}

	/*
	 * C3. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to
	 * 1 month in advance. We do not allow reservations that include days that are
	 * more than a month in advance.
	 */
	@Test(expected = DateRangeException.class)
	public void testModificationInvalidEndDate() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(4);

		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationDao.save(reservation);

		Optional<Reservation> original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isPresent());

		// Move Reservation to today
		LocalDate newStartDate = LocalDate.now().plusMonths(1);
		LocalDate newEndDate = LocalDate.now().plusMonths(1).plusDays(2);
		Reservation modification = new Reservation("Brian Leung", "brian.leung@mail.com", newStartDate, newEndDate);

		mUpdateReservationJobFactory.getJob(reservation.getId(), modification).call();
	}

	@Test
	public void testModificationAllFieldsSuccess() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(4);

		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationDao.save(reservation);

		Optional<Reservation> original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isPresent());

		// Move Reservation to today
		LocalDate newStartDate = LocalDate.now().plusDays(15);
		LocalDate newEndDate = LocalDate.now().plusDays(17);
		Reservation modification = new Reservation("Ben Greg", "ben.greg@mail.com", newStartDate, newEndDate);

		Reservation modified = mUpdateReservationJobFactory.getJob(reservation.getId(), modification).call();
		
		assertNotNull(modified);
		assertEquals(reservation.getId(), modified.getId()); //Verify that the UUID has not changed
		assertEquals(modification.getName(), modified.getName());
		assertEquals(modification.getEmail(), modified.getEmail());
		assertEquals(newStartDate, modified.getStartDate());
		assertEquals(newEndDate, modified.getEndDate());
	}

	@Test( expected = ReservationNotFoundException.class )
	public void testInvalidModificationUUID() throws Exception {
		LocalDate newStartDate = LocalDate.now().plusDays(15);
		LocalDate newEndDate = LocalDate.now().plusDays(17);
		Reservation modification = new Reservation("Ben Greg", "ben.greg@mail.com", newStartDate, newEndDate);

		Reservation modified = mUpdateReservationJobFactory.getJob(UUID.randomUUID(), modification).call();
		
		assertNotNull(modified);
	}
	
}
