package org.coffeehouse.home.reservation.data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationH2DaoImpl implements ReservationDao {

	private final ReservationRepository repository;
	
	@Autowired
	private ReservationCache mReservationCache;

	public ReservationH2DaoImpl(ReservationRepository repository) {
		this.repository = repository;
	}

	@Override
	public Reservation save(Reservation reservation) {
		mReservationCache.add(reservation);
		Reservation saved = repository.save(reservation);
		log.debug("Saved reservation with uuid: " + saved.getId());
		return saved;
	}

	@Override
	public Reservation modify(UUID uuid, Reservation modified) throws ReservationNotFoundException {
		log.debug("Modifying reservation with uuid: " + uuid);
		try {
			return repository.findById(uuid).map(reservation -> {
				mReservationCache.remove(reservation);
				reservation.setName(modified.getName());
				reservation.setEmail(modified.getEmail());
				reservation.setStartDate(modified.getStartDate());
				reservation.setEndDate(modified.getEndDate());
				mReservationCache.add(reservation);
				return repository.save(reservation);
			}).orElseThrow(ReservationNotFoundException::new);
		} catch( ReservationNotFoundException exception ) {
			log.error("Failed to modify reservation with uuid: " + uuid);
			throw new ReservationNotFoundException();
		}
	}

	@Override
	public void cancel(UUID uuid) throws ReservationNotFoundException {
		log.debug("Cancelling reservation with uuid: " + uuid);
		Optional<Reservation> reservation = repository.findById(uuid);
		if (reservation.isEmpty()) {
			log.error("Failed to cancel reservation with uuid: " + uuid);
			throw new ReservationNotFoundException();
		}
		mReservationCache.remove(reservation.get());
		repository.deleteById(uuid);
	}

	@Override
	public Optional<Reservation> findById(UUID uuid) {
		return repository.findById(uuid);
	}

	@Override
	public Collection<Reservation> findReservationInRange(LocalDate startDate, LocalDate endDate) {
		return repository.findReservationsWithinDates(startDate, endDate);
	}

}
