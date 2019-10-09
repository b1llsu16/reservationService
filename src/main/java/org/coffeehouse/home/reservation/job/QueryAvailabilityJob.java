package org.coffeehouse.home.reservation.job;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.coffeehouse.home.reservation.ReservationConfig;
import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationCache;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;

public class QueryAvailabilityJob implements ExecutableJob<List<LocalDate>> {

	public static class QueryAvailabilityJobFactory {

		@Autowired
		public ReservationDao mReservationDao;

		@Autowired
		public LocalDateHelper mLocalDateHelper;
		
		@Autowired
		public ReservationCache mReservationCache;

		public QueryAvailabilityJob getJob(LocalDate startDate, LocalDate endDate) {
			return new QueryAvailabilityJob(startDate, endDate, mReservationDao, mLocalDateHelper, mReservationCache);

		}
	}

	@NotNull
	public ReservationDao mReservationDao;
	@NotNull
	public LocalDateHelper mLocalDateHelper;
	@NotNull
	private LocalDate mStartDate;
	@NotNull
	private LocalDate mEndDate;
	@NotNull
	public ReservationCache mReservationCache;

	public QueryAvailabilityJob(LocalDate startDate, LocalDate endDate, ReservationDao reservationDao,
			LocalDateHelper localDateHelper, ReservationCache reservationCache) {
		mStartDate = startDate;
		mEndDate = endDate;
		mReservationDao = reservationDao;
		mLocalDateHelper = localDateHelper;
		mReservationCache = reservationCache;
	}

	@Override
	public List<LocalDate> call() {

		if ( ReservationConfig.ENABLE_RESERVATION_CACHING )
		{
			return mReservationCache.getAvailability(mStartDate, mEndDate);
		} else {
			Collection<Reservation> reservations = mReservationDao.findReservationInRange(mStartDate, mEndDate);
			List<LocalDate> availableDates = mLocalDateHelper.getDatesWithinRange(mStartDate, mEndDate);
			for (Reservation reservation : reservations) {
				mLocalDateHelper.getReservedDates(reservation).stream().forEach(date -> availableDates.remove(date));
			}
			return availableDates;
		}
	}
}
