package reservation.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationCache;
import org.coffeehouse.home.reservation.data.ReservationH2DaoImpl;
import org.coffeehouse.home.reservation.exceptions.ReservationAlreadyExistsException;
import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
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
public class TestReservationCache {

	@Autowired
	private ReservationH2DaoImpl mReservationH2DaoImpl;

	@Autowired
	private ReservationCache mReservationCache;

	@Autowired
	private QueryAvailabilityJobFactory mQueryAvailabilityJobFactory;

	@Test
	public void TestReservationCacheNewReservation() throws ReservationAlreadyExistsException {
		LocalDate dummy = LocalDate.now();
		Reservation newReservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);
		mReservationH2DaoImpl.save(newReservation);

		Optional<Reservation> saved = mReservationH2DaoImpl.findById(newReservation.getId());
		assertTrue(saved.isPresent());

		Optional<Reservation> cached = mReservationCache.get(dummy);
		assertTrue(cached.isPresent());

		assertEquals(saved.get().getName(), cached.get().getName());
		assertEquals(saved.get().getEmail(), cached.get().getEmail());
		assertEquals(saved.get().getId(), cached.get().getId());
		assertEquals(saved.get().getStartDate(), cached.get().getStartDate());
		assertEquals(saved.get().getEndDate(), cached.get().getEndDate());
	}

	@Test
	public void TestReservationCacheModification()
			throws ReservationAlreadyExistsException, ReservationNotFoundException {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(2);
		Reservation oldReservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);
		mReservationH2DaoImpl.save(oldReservation);

		// Create modified reservation.
		LocalDate newDate = LocalDate.now().plusDays(5);
		Reservation modifiedReservation = new Reservation("Brian Leung", "brian.leung@mail.com", newDate, newDate);
		// Perform modification
		Reservation modified = mReservationH2DaoImpl.modify(oldReservation.getId(), modifiedReservation);

		// Check that the first Dates are freed
		Optional<Reservation> cached = mReservationCache.get(startDate);
		assertTrue(cached.isEmpty());
		cached = mReservationCache.get(endDate);
		assertTrue(cached.isEmpty());

		// Check that the reservation is not on the new date
		Optional<Reservation> newcached = mReservationCache.get(newDate);
		assertTrue(newcached.isPresent());

		assertEquals(modified.getName(), newcached.get().getName());
		assertEquals(modified.getEmail(), newcached.get().getEmail());
		assertEquals(modified.getId(), newcached.get().getId());
		assertEquals(modified.getStartDate(), newcached.get().getStartDate());
		assertEquals(modified.getEndDate(), newcached.get().getEndDate());
	}

	@Test
	public void TestReservationCacheCancellation()
			throws ReservationNotFoundException, ReservationAlreadyExistsException {
		LocalDate dummy = LocalDate.now();
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);
		Reservation saved = mReservationH2DaoImpl.save(reservation);

		Optional<Reservation> cached = mReservationCache.get(dummy);
		assertTrue(cached.isPresent());

		// Perform cancel
		mReservationH2DaoImpl.cancel(reservation.getId());

		// Verify that the id no longer exists
		Optional<Reservation> searchById = mReservationH2DaoImpl.findById(reservation.getId());
		assertFalse(searchById.isPresent());

		Optional<Reservation> cancelled_cache = mReservationCache.get(dummy);
		assertTrue(cancelled_cache.isEmpty());
	}

	@Test
	public void TestReservationCacheGetAvailable() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);

		mReservationH2DaoImpl.save(reservation);

		LocalDate startRange = LocalDate.now().plusDays(1);
		LocalDate endRange = startRange.plusMonths(1);

		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startRange, endRange).call();
		List<LocalDate> cache_availability = mReservationCache.getAvailability();
		assertFalse(availability.isEmpty());
		assertFalse(cache_availability.isEmpty());
		assertEquals(availability.size(), cache_availability.size());

		availability.stream().forEach(date -> assertTrue(cache_availability.contains(date)));
	}
	
	@Test
	public void TestReservationCacheGetAvailableWithRange() throws ReservationAlreadyExistsException {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);

		mReservationH2DaoImpl.save(reservation);

		// Assert when entire range is occupied, both return empty lists
		List<LocalDate> availability = mQueryAvailabilityJobFactory.getJob(startDate, endDate).call();
		List<LocalDate> cache_availability = mReservationCache.getAvailability(startDate, endDate);
		assertTrue(availability.isEmpty());
		assertTrue(cache_availability.isEmpty());
		assertEquals(availability.size(), cache_availability.size());
		
		// Assert when we expand the range, they both return the same result
		List<LocalDate> availability2 = mQueryAvailabilityJobFactory.getJob(startDate, endDate.plusDays(5)).call();
		List<LocalDate> cache_availability2 = mReservationCache.getAvailability(startDate, endDate.plusDays(5));
		assertFalse(availability2.isEmpty());
		assertFalse(cache_availability2.isEmpty());
		assertEquals(availability2.size(), cache_availability2.size());

		availability2.stream().forEach(date -> assertTrue(cache_availability2.contains(date)));
	}

}
