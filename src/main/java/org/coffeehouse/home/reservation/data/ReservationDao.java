package org.coffeehouse.home.reservation.data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;

public interface ReservationDao {

	public Reservation save( Reservation reservation );
	
	public Reservation modify( UUID uuid, Reservation reservation ) throws ReservationNotFoundException ;
	
	public void cancel( UUID uuid ) throws ReservationNotFoundException ;
	
	public Optional<Reservation> findById( UUID uuid );
	
	public Collection<Reservation> findReservationInRange( LocalDate startDate, LocalDate endDate );
}
