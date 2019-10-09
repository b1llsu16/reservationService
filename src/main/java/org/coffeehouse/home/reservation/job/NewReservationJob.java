package org.coffeehouse.home.reservation.job;

import javax.validation.constraints.NotNull;

import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.exceptions.DateRangeException;
import org.coffeehouse.home.reservation.job.LocalDateHelper.RESULT;
import org.springframework.beans.factory.annotation.Autowired;

public class NewReservationJob implements ExecutableJob<Reservation> {

	public static class NewReservationJobFactory {

		@Autowired
		public ReservationDao mReservationDao;

		@Autowired
		public LocalDateHelper mLocalDateHelper;

		public NewReservationJob getJob(Reservation reservation) {
			return new NewReservationJob(reservation, mReservationDao, mLocalDateHelper);

		}
	}

	@NotNull
	private ReservationDao mReservationDao;
	@NotNull
	public LocalDateHelper mLocalDateHelper;
	@NotNull
	private Reservation mReservation;

	public NewReservationJob(Reservation reservation, ReservationDao reservationDao, LocalDateHelper localDateHelper) {
		mReservation = reservation;
		mReservationDao = reservationDao;
		mLocalDateHelper = localDateHelper;
	}

	@Override
	public Reservation call() throws Exception {
		RESULT validation = mLocalDateHelper.isReservationValid(mReservation);
		switch (validation) {
		case INVALID_START:
			throw new DateRangeException(
					"Invalid start date. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. ");
		case INVALID_END:
			throw new DateRangeException(
					"Invalid end date. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. ");
		case INVALID_RANGE:
			throw new DateRangeException("Invalid date range. The campsite can be reserved for max 3 days.");
		case DATE_IN_RANGE_UNAVAILABLE:
			throw new DateRangeException("One ore more requested dates in desired range have already been reserved.");
		default:
			synchronized (mReservationDao) {
				return mReservationDao.save(mReservation);
			}
		}

	}
}
