package integrationtesting;

import java.time.LocalDate;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.coffeehouse.home.reservation.data.Reservation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import integrationtesting.StatisticsHandler.RequestType;

public class ReservationClient implements Runnable {

	private final String uri = "http://localhost:8080/reservation/reserve";

	private int mClientId;

	public ReservationClient(int clientId) {
		mClientId = clientId;
	}

	@Override
	public void run() {
		while (!StatisticsHandler.stop) {
			System.out.println(RequestType.reserve + " " + mClientId);
			LocalDate startDate = LocalDate.now().plusDays(new Random().nextInt(30));
			LocalDate endDate = startDate.plusDays(new Random().nextInt(5));
			Reservation reservation = new Reservation(RandomStringUtils.randomAlphabetic(10),RandomStringUtils.randomAlphabetic(10), startDate, endDate);

			RestTemplate restTemplate = new RestTemplate();
			try {
				StatisticsHandler.inc(RequestType.reserve);
				ResponseEntity<Reservation> result = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(reservation), Reservation.class);
				StatisticsHandler.inc(RequestType.reserve, result.getStatusCode().toString());
				StatisticsHandler.addReservation(result.getBody());
			} catch (HttpStatusCodeException e) {
				ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
				StatisticsHandler.inc(RequestType.reserve, String.valueOf(e.getRawStatusCode()));
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
