package reservation.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
import org.coffeehouse.home.reservation.job.CancelReservationJob.CancelReservationJobFactory;
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
public class TestCancelReservationJob {

	@Autowired
	private CancelReservationJobFactory mCancelReservationJobFactory;

	@Autowired
	private ReservationDao mReservationDao;

	@Autowired
	private QueryAvailabilityJobFactory mQueryAvailabilityJobFactory;

	@Test
	public void TestCancelReservation() throws Exception {
		LocalDate startDate = LocalDate.now().plusDays(2);
		LocalDate endDate = LocalDate.now().plusDays(4);

		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationDao.save(reservation);

		Optional<Reservation> original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isPresent());

		// Checking availability for this exact time frame should return nothing.
		List<LocalDate> availableDates = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		assertTrue(availableDates.isEmpty());

		mCancelReservationJobFactory.getJob(original.get().getId()).call();
		original = mReservationDao.findById(reservation.getId());
		assertTrue(original.isEmpty());

		// Checking availability for this exact time frame after cancellation should return all dates.
		availableDates = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		assertEquals(3, availableDates.size());
		assertEquals(startDate, availableDates.get(0));
		assertEquals(startDate.plusDays(1), availableDates.get(1));
		assertEquals(endDate, availableDates.get(2));
	}
	
	@Test( expected = ReservationNotFoundException.class )
	public void testCancelInvalidReservation() throws Exception {
		mCancelReservationJobFactory.getJob(UUID.randomUUID()).call();
	}

}
