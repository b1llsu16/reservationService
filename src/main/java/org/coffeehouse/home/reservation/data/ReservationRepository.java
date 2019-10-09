package org.coffeehouse.home.reservation.data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

	@Query("select r from Reservation r where not ( r.startDate > ?2 or r.endDate < ?1 )")
	Collection<Reservation> findReservationsWithinDates(LocalDate startDate, LocalDate endDate);

}