package integrationtesting;

import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

import org.coffeehouse.home.reservation.data.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import integrationtesting.StatisticsHandler.RequestType;

public class ModifyClient implements Runnable {

	private final String uri = "http://localhost:8080/reservation/modify/%s";

	private int mClientId;

	public ModifyClient(int clientId) {
		mClientId = clientId;
	}
	
	@Override
	public void run() {
		while (!StatisticsHandler.stop) {
			System.out.println(RequestType.modify + " " + mClientId);

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
			if (new Random().nextInt(100) > 90) {
				uuid = UUID.randomUUID();
				Reservation save = reservation;
				StatisticsHandler.addReservation(save);
				reservation.setId(uuid);
			} else {
				uuid = reservation.getId();
				LocalDate startDate = LocalDate.now().plusDays(new Random().nextInt(30));
				LocalDate endDate = startDate.plusDays(new Random().nextInt(5));
				reservation.setStartDate(startDate);
				reservation.setEndDate(endDate);
			}
			RestTemplate restTemplate = new RestTemplate();
			try {
				StatisticsHandler.inc(RequestType.modify);
				restTemplate.put(String.format(uri, uuid.toString()), reservation);
				StatisticsHandler.inc(RequestType.modify, "200");
				StatisticsHandler.addReservation(reservation);
			} catch (HttpStatusCodeException e) {
				ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
				StatisticsHandler.inc(RequestType.modify, String.valueOf(e.getRawStatusCode()));
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
