package org.coffeehouse.home.reservation.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.coffeehouse.home.reservation.job.LocalDateHelper;

public class ReservationCache {

	@NotNull
	private LocalDateHelper mLocalDateHelper;

	@NotNull
	private Map<LocalDate, Reservation> mReservationMap;

	public ReservationCache(Map<LocalDate, Reservation> reservationMap, LocalDateHelper localDateHelper) {
		mReservationMap = reservationMap;
		mLocalDateHelper = localDateHelper;
	}

	public void add(Reservation reservation) {
		List<LocalDate> reserveDates = mLocalDateHelper.getDatesWithinRange(reservation.getStartDate(),
				reservation.getEndDate());
		reserveDates.stream().forEach(date -> mReservationMap.put(date, reservation));
	}

	public void remove(Reservation reservation) {
		mReservationMap.keySet().stream().filter(date -> mReservationMap.get(date) != null)
				.filter(date -> mReservationMap.get(date).getId() == reservation.getId())
				.forEach(date -> mReservationMap.put(date, null));
	}

	public List<LocalDate> getAvailability() {
		return mReservationMap.keySet().stream().sorted().filter(date -> mReservationMap.get(date) == null)
				.collect(Collectors.toList());
	}

	public List<LocalDate> getAvailability(@NotNull LocalDate startRange, @NotNull LocalDate endRange) {
		return mReservationMap.keySet().stream().sorted().filter(date -> mReservationMap.get(date) == null)
				.filter(date -> (date.equals(startRange) || date.isAfter(startRange))
						&& (date.equals(endRange) || date.isBefore(endRange)))
				.collect(Collectors.toList());
	}

	public Optional<Reservation> get(LocalDate date) {
		return Optional.ofNullable(mReservationMap.get(date));
	}

	public void rotate() {
		mReservationMap.remove(LocalDate.now()); // Remove Today
		mReservationMap.put(LocalDate.now().plusMonths(1), null); // Add one month from Tomorrow
	}
}
