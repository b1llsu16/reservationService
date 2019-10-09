package org.coffeehouse.home.reservation.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/*
 * This scheduled task runs every morning at 12:05. It rotates out today from
 * the ReservationCache as it is no longer eligible for availability. It adds the 
 * newly available reservation date, today plus 1 month;
 */
@Component
@Slf4j
public class RotateReservationCacheTask {

	@Autowired
	private ReservationCache mReservationCache;
	
	@Scheduled(cron="5 0 * * *")
	public void rotateReservationCache() {
		log.info("Rotating Cache.");
		mReservationCache.rotate();
		
	}
}
