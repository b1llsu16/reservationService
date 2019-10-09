package integrationtesting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import integrationtesting.StatisticsHandler.RequestType;

@SpringBootApplication
public class ReservationIntegrationTesting {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		
		for ( int i = 0; i < 5; i ++ ) {
			executor.execute(new AvailabilityClient(i));
		}
		for ( int i = 0; i < 7; i ++ ) {
			executor.execute(new ReservationClient(i));
		}
		for ( int i = 0; i < 3; i ++ ) {
			executor.execute(new CancelClient(i));
		}
		for ( int i = 0; i < 5; i ++ ) {
			executor.execute(new ModifyClient(i));
		}
		
        Thread.sleep(15_000);
         
        StatisticsHandler.stop = true;
		StatisticsHandler.getStats(RequestType.availability);
		StatisticsHandler.getStats(RequestType.reserve);
		StatisticsHandler.getStats(RequestType.modify);
		StatisticsHandler.getStats(RequestType.cancel);
	}

}
