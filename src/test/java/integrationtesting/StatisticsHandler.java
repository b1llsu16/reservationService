package integrationtesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.coffeehouse.home.reservation.data.Reservation;

public class StatisticsHandler {

	public static boolean stop = false;
	
	public static enum RequestType {
		reserve, availability, modify, cancel
	}
	
	private static List<Reservation> reservations = new ArrayList<Reservation>();

	private static int reservationRequest = 0;
	private static int availabilityRequest = 0;
	private static int modifyRequest = 0;
	private static int cancelRequest = 0;

	private static Map<String, Integer> reservationResponseMap = new HashMap<String, Integer>();
	private static Map<String, Integer> availabilityResponseMap = new HashMap<String, Integer>();
	private static Map<String, Integer> modifyResponseMap = new HashMap<String, Integer>();
	private static Map<String, Integer> cancelResponseMap = new HashMap<String, Integer>();

	public static void inc(RequestType requestType) {
		switch (requestType) {
		case reserve:
			reservationRequest++;
			break;
		case availability:
			availabilityRequest++;
			break;
		case modify:
			modifyRequest++;
			break;
		case cancel:
			cancelRequest++;
			break;
		}
	}

	public static void inc(RequestType requestType, String responseCode) {
		switch (requestType) {
		case reserve:
			reservationResponseMap.put(responseCode, reservationResponseMap.getOrDefault(responseCode, 0) + 1);
			break;
		case availability:
			availabilityResponseMap.put(responseCode, availabilityResponseMap.getOrDefault(responseCode, 0) + 1);
			break;
		case modify:
			modifyResponseMap.put(responseCode, modifyResponseMap.getOrDefault(responseCode, 0) + 1);
			break;
		case cancel:
			cancelResponseMap.put(responseCode, cancelResponseMap.getOrDefault(responseCode, 0) + 1);
			break;
		}
	}

	public static void getStats(RequestType requestType) {
		switch (requestType) {
		case reserve:
			System.out.println(requestType.toString() + " total calls: " + reservationRequest);
			printMap(reservationResponseMap);
			break;
		case availability:
			System.out.println(requestType.toString() + " total calls: " + availabilityRequest);
			printMap(availabilityResponseMap);
			break;
		case modify:
			System.out.println(requestType.toString() + " total calls: " + modifyRequest);
			printMap(modifyResponseMap);
			break;
		case cancel:
			System.out.println(requestType.toString() + " total calls: " + cancelRequest);
			printMap(cancelResponseMap);
			break;
		}
	}

	private static void printMap(Map<String, Integer> mapToPrint) {
		for (String responseCode : mapToPrint.keySet()) {
			System.out.println("responseCode: " + responseCode + " seen " + mapToPrint.get(responseCode));
		}
	}
	
	public static void addReservation( Reservation reservation ) {
		reservations.add(reservation);
	}
	
	public static Reservation getReservation() {
		synchronized (reservations) {
			if (reservations.size() < 1) {
				return null;
			}
			int rnd = new Random().nextInt(reservations.size());
			return reservations.remove(rnd);
		}
	}
}
