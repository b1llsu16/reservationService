package reservation.job;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.coffeehouse.home.reservation.ReservationApplication;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.job.LocalDateHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReservationApplication.class)
public class TestLocalDateHelper {

	@Autowired
	public LocalDateHelper mLocalDateHelper;

	@Test
	public void testReservedDatesWhereStartDateEqualsEndDate() {
		LocalDate dummy = LocalDate.now();
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", dummy, dummy);

		List<LocalDate> reservedDates = mLocalDateHelper.getReservedDates(reservation);

		assertEquals(1, reservedDates.size());
		assertEquals(dummy, reservedDates.get(0));
	}

	@Test
	public void testReservedDates() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = LocalDate.now().plusDays(2);
		Reservation reservation = new Reservation("Brian Leung", "brian.leung@mail.com", startDate, endDate);

		List<LocalDate> reservedDates = mLocalDateHelper.getReservedDates(reservation);

		assertEquals(3, reservedDates.size());
		assertEquals(startDate, reservedDates.get(0));
		assertEquals(startDate.plusDays(1), reservedDates.get(1));
		assertEquals(endDate, reservedDates.get(2));
	}

}
