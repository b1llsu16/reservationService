package integrationtesting;

import java.util.Random;
import java.util.UUID;

import org.coffeehouse.home.reservation.data.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import integrationtesting.StatisticsHandler.RequestType;

public class CancelClient implements Runnable {
	private final String uri = "http://localhost:8080/reservation/cancel/%s";

	private int mClientId;

	public CancelClient(int clientId) {
		mClientId = clientId;
	}

	@Override
	public void run() {
		while (!StatisticsHandler.stop) {
			System.out.println(RequestType.cancel + " " + mClientId);

			UUID uuid;
			Reservation reservation = StatisticsHandler.getReservation();
			if (reservation == null) {
				try {
					Thread.sleep(new Random().nextInt(500));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			if (new Random().nextInt(100) > 95) {
				uuid = UUID.randomUUID();
			} else {
				uuid = reservation.getId();
			}
			RestTemplate restTemplate = new RestTemplate();
			try {
				StatisticsHandler.inc(RequestType.cancel);
				restTemplate.delete ( String.format( uri, uuid.toString() ) );
				StatisticsHandler.inc(RequestType.cancel, "200");
			} catch (HttpStatusCodeException e) {
				ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
				StatisticsHandler.inc(RequestType.cancel, String.valueOf(e.getRawStatusCode()));
			}
			try {
				Thread.sleep(new Random().nextInt(500));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
