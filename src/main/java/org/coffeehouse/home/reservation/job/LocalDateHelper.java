package org.coffeehouse.home.reservation.job;

import static org.coffeehouse.home.reservation.ReservationConfig.MAX_RESERVATION_LENGTH_DAYS;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;

public class LocalDateHelper {

	@Autowired
	public ReservationDao mReservationDao;
	
	public static enum RESULT {
		OK, INVALID_START, INVALID_END, INVALID_RANGE, DATE_IN_RANGE_UNAVAILABLE
	}

	public List<LocalDate> getReservedDates(Reservation reservation) {
		return getDatesWithinRange(reservation.getStartDate(), reservation.getEndDate());
	}

	public List<LocalDate> getDatesWithinRange(LocalDate startDate, LocalDate endDate) {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		LocalDate sdate = startDate;
		while (!sdate.isAfter(endDate)) {
			dates.add(sdate);
			sdate = sdate.plusDays(1);
		}
		return dates;
	}

	public RESULT isReservationValid(@NotNull Reservation reservation) {
		return areDatesValid(reservation.getStartDate(), reservation.getEndDate());
	}

	public RESULT areDatesValid(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
		LocalDate validStart = LocalDate.now().plusDays(1);
		LocalDate validEnd = LocalDate.now().plusMonths(1);

		if (startDate.isBefore(validStart) || startDate.isAfter(validEnd)) {
			return RESULT.INVALID_START;
		}
		if (endDate.isBefore(validStart) || endDate.isAfter(validEnd)) {
			return RESULT.INVALID_END;
		}
		// between() includes the startDate but not the endDate so adding 1.
		long length = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (length > MAX_RESERVATION_LENGTH_DAYS) {
			return RESULT.INVALID_RANGE;
		}
		Collection<Reservation> reservations = mReservationDao.findReservationInRange(startDate, endDate);
		if ( !reservations.isEmpty() ) {
			return RESULT.DATE_IN_RANGE_UNAVAILABLE;
		}

		return RESULT.OK;
	}

}
