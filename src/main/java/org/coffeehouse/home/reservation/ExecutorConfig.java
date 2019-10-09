package org.coffeehouse.home.reservation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {
	
	private static final Integer MAX_QUERY_THREADS = 5;
	
	@Bean("reservationUpdateExecutor")
	public ExecutorService getUpdateReservationExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	@Bean("reservationQueryExecutor")
	public ExecutorService getQueryReservationExecutor() {
		return Executors.newFixedThreadPool(MAX_QUERY_THREADS);
	}
}
