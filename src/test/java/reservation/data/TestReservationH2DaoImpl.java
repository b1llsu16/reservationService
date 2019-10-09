package reservation.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationH2DaoImpl;
import org.coffeehouse.home.reservation.exceptions.ReservationAlreadyExistsException;
import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
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
public class TestReservationH2DaoImpl {

	@Autowired
	private ReservationH2DaoImpl mReservationH2DaoImpl;

	@Test
	public void insertNewReservation() throws ReservationAlreadyExistsException {
		LocalDate dummy = LocalDate.now();
		Reservation newReservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);
		Reservation saved = mReservationH2DaoImpl.save(newReservation);

		assertEquals(newReservation.getName(), saved.getName());
		assertEquals(newReservation.getEmail(), saved.getEmail());
		assertEquals(newReservation.getId(), saved.getId());
		assertEquals(dummy, saved.getStartDate());
		assertEquals(dummy, saved.getEndDate());
	}

	@Test
	public void modifyExistingReservation() throws ReservationNotFoundException, ReservationAlreadyExistsException {
		LocalDate dummy = LocalDate.now();
		Reservation oldReservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);
		Reservation saved = mReservationH2DaoImpl.save(oldReservation);

		assertEquals(oldReservation.getName(), saved.getName());
		assertEquals(oldReservation.getEmail(), saved.getEmail());
		assertEquals(oldReservation.getId(), saved.getId());
		assertEquals(dummy, saved.getStartDate());
		assertEquals(dummy, saved.getEndDate());

		// Create modified reservation.
		LocalDate dummy2 = LocalDate.now().plusDays(5);
		Reservation modifiedReservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy2, dummy2);
		// Perform modification
		Reservation modified = mReservationH2DaoImpl.modify(oldReservation.getId(), modifiedReservation);

		assertEquals(modifiedReservation.getName(), modified.getName());
		assertEquals(modifiedReservation.getEmail(), modified.getEmail());
		assertEquals(oldReservation.getId(), modified.getId()); // modified maintains the same UUID
		assertEquals(dummy2, modified.getStartDate());
		assertEquals(dummy2, modified.getEndDate());

		// Search by original reservations id to ensure that it has been modified.
		Optional<Reservation> searchById = mReservationH2DaoImpl.findById(oldReservation.getId());
		assertTrue(searchById.isPresent());
		assertEquals(modifiedReservation.getName(), searchById.get().getName());
		assertEquals(modifiedReservation.getEmail(), searchById.get().getEmail());
		assertEquals(oldReservation.getId(), searchById.get().getId()); // searchById maintains the same UUID
		assertEquals(dummy2, searchById.get().getStartDate());
		assertEquals(dummy2, searchById.get().getEndDate());
	}

	@Test(expected = ReservationNotFoundException.class)
	public void modifyNotFoundReservation() throws ReservationNotFoundException {
		LocalDate dummy = LocalDate.now();
		UUID dummyUUID = UUID.randomUUID();
		Reservation modifiedReservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);

		mReservationH2DaoImpl.modify(dummyUUID, modifiedReservation); // should throw
	}

	@Test
	public void cancelExistingReservation() throws ReservationNotFoundException, ReservationAlreadyExistsException {
		LocalDate dummy = LocalDate.now();
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);
		Reservation saved = mReservationH2DaoImpl.save(reservation);

		assertEquals(reservation.getName(), saved.getName());
		assertEquals(reservation.getEmail(), saved.getEmail());
		assertEquals(reservation.getId(), saved.getId());
		assertEquals(dummy, saved.getStartDate());
		assertEquals(dummy, saved.getEndDate());

		// Perform cancel
		mReservationH2DaoImpl.cancel(reservation.getId());

		// Verify that the id no longer exists
		Optional<Reservation> searchById = mReservationH2DaoImpl.findById(reservation.getId());
		assertFalse(searchById.isPresent());
	}

	@Test(expected = ReservationNotFoundException.class)
	public void cancelNotFoundReservation() throws ReservationNotFoundException {
		UUID dummyUUID = UUID.randomUUID();
		mReservationH2DaoImpl.cancel(dummyUUID); // should throw
	}

	@Test
	public void returnReservationsInRange() throws ReservationAlreadyExistsException {
		LocalDate searchStartDate = LocalDate.now(); // Start at 0
		LocalDate searchEndDate = LocalDate.now().plusDays(15); // Search 15 Days ahead

		// Reservations matching the startDate and are less than the end date
		LocalDate startDate1 = LocalDate.now(); // at 0
		LocalDate endDate1 = LocalDate.now(); // at 0
		Reservation found1 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate1, endDate1);

		// Reservations within the search range are found
		LocalDate startDate2 = LocalDate.now().plusDays(5); // at 5
		LocalDate endDate2 = LocalDate.now().plusDays(8); // at 8
		Reservation found2 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate2, endDate2);

		// Reservations that start within the range but end outside are found
		LocalDate startDate3 = LocalDate.now().plusDays(14); // at 14
		LocalDate endDate3 = LocalDate.now().plusDays(17); // at 17, outside range
		Reservation found3 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate3, endDate3);

		// Reservations that are outside the range are not found
		LocalDate startDate4 = LocalDate.now().plusDays(16); // at 16
		LocalDate endDate4 = LocalDate.now().plusDays(18); // at 18, outside range
		Reservation notFound4 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate4, endDate4);

		// Reservations that span the entire range are found
		LocalDate startDate5 = LocalDate.now().minusDays(1); // at -1
		LocalDate endDate5 = LocalDate.now().plusDays(16); // at 16, outside range
		Reservation found5 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate5, endDate5);

		// Reservations before the range are not found
		LocalDate startDate6 = LocalDate.now().minusDays(5); // at -1
		LocalDate endDate6 = LocalDate.now().minusDays(2); // at 16, outside range
		Reservation notFound6 = new Reservation("Brian Leung", "brian.leung@mail.com", startDate6, endDate6);

		mReservationH2DaoImpl.save(found1);
		mReservationH2DaoImpl.save(found2);
		mReservationH2DaoImpl.save(found3);
		mReservationH2DaoImpl.save(notFound4);
		mReservationH2DaoImpl.save(found5);
		mReservationH2DaoImpl.save(notFound6);

		Collection<Reservation> reservations = mReservationH2DaoImpl.findReservationInRange(searchStartDate,
				searchEndDate);
		assertEquals(4, reservations.size());
	}
}
