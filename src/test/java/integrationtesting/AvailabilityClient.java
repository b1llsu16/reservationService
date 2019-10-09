package integrationtesting;

import java.util.Arrays;
import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import integrationtesting.StatisticsHandler.RequestType;

public class AvailabilityClient implements Runnable {

	final String uri = "http://localhost:8080/reservation/availability";

	private int mClientId;
	
	public AvailabilityClient(int clientId){
		mClientId = clientId;
	}
	
	@Override
	public void run() {
		while (!StatisticsHandler.stop) {
			System.out.println(RequestType.availability + " " + mClientId);
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			StatisticsHandler.inc(RequestType.availability);

			ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

			StatisticsHandler.inc(RequestType.availability, result.getStatusCode().toString());
			try {
				Thread.sleep(new Random().nextInt(500));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
