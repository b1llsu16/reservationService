package org.coffeehouse.home.reservation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationCache;
import org.coffeehouse.home.reservation.data.ReservationDao;
import org.coffeehouse.home.reservation.data.ReservationH2DaoImpl;
import org.coffeehouse.home.reservation.data.ReservationRepository;
import org.coffeehouse.home.reservation.job.CancelReservationJob.CancelReservationJobFactory;
import org.coffeehouse.home.reservation.job.LocalDateHelper;
import org.coffeehouse.home.reservation.job.NewReservationJob.NewReservationJobFactory;
import org.coffeehouse.home.reservation.job.QueryAvailabilityJob.QueryAvailabilityJobFactory;
import org.coffeehouse.home.reservation.job.UpdateReservationJob.UpdateReservationJobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfig {

	public final static Integer MAX_RESERVATION_LENGTH_DAYS = 3;
	public final static Boolean ENABLE_RESERVATION_CACHING = false;
	
	private final ReservationRepository mReservationRepository;

	public ReservationConfig(ReservationRepository repository) {
		mReservationRepository = repository;
	}

	@Bean
	public ReservationDao getReservationDao() {
		return new ReservationH2DaoImpl(mReservationRepository);
	}

	@Bean
	public QueryAvailabilityJobFactory getQueryAvailabilityJobFactory() {
		return new QueryAvailabilityJobFactory();
	}

	@Bean
	public NewReservationJobFactory getNewReservationJobFactory() {
		return new NewReservationJobFactory();
	}

	@Bean
	public UpdateReservationJobFactory getUpdateReservationJobFactory() {
		return new UpdateReservationJobFactory();
	}

	@Bean
	public CancelReservationJobFactory getCancelReservationJobFactory() {
		return new CancelReservationJobFactory();
	}

	@Bean
	public LocalDateHelper getLocalDateHelper() {
		return new LocalDateHelper();
	}

	@Bean
	public ReservationCache getReservationCacheMap() {
		LocalDate mStartDate = LocalDate.now().plusDays(1);
		LocalDate mEndDate = LocalDate.now().plusMonths(1);
		Map<LocalDate, Reservation> reservationMap = new HashMap<LocalDate, Reservation>();

		getLocalDateHelper().getDatesWithinRange(mStartDate, mEndDate).stream()
				.forEach(date -> reservationMap.put(date, null));

		Collection<Reservation> reservations = getReservationDao().findReservationInRange(mStartDate, mEndDate);

		for (Reservation reservation : reservations) {
			getLocalDateHelper().getReservedDates(reservation).stream()
					.forEach(date -> reservationMap.put(date, reservation));
		}
		return new ReservationCache(reservationMap, getLocalDateHelper());
	}
}
