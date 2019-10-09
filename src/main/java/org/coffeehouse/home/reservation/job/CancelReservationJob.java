package org.coffeehouse.home.reservation.job;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.coffeehouse.home.reservation.data.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;

public class CancelReservationJob implements ExecutableJob<Void>{

	public static class CancelReservationJobFactory {
		
		@Autowired
		public ReservationDao mReservationDao;
		
		public CancelReservationJob getJob(UUID id) {
			return new CancelReservationJob(id, mReservationDao);
		}
	}
	
	@NotNull
	private ReservationDao mReservationDao;
	@NotNull
	private UUID mUUID;
	
	public CancelReservationJob(UUID id, ReservationDao reservationDao) {
		mUUID = id;
		mReservationDao = reservationDao;
	}

	@Override
	public Void call() throws Exception {
		synchronized (mReservationDao) {
			mReservationDao.cancel(mUUID);
		}
		return null;
	}

}
