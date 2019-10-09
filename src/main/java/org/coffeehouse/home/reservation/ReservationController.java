package org.coffeehouse.home.reservation;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.coffeehouse.home.reservation.data.Reservation;
import org.coffeehouse.home.reservation.data.ReservationRepository;
import org.coffeehouse.home.reservation.exceptions.DateRangeException;
import org.coffeehouse.home.reservation.exceptions.ReservationNotFoundException;
import org.coffeehouse.home.reservation.job.CancelReservationJob.CancelReservationJobFactory;
import org.coffeehouse.home.reservation.job.LocalDateHelper;
import org.coffeehouse.home.reservation.job.LocalDateHelper.RESULT;
import org.coffeehouse.home.reservation.job.NewReservationJob.NewReservationJobFactory;
import org.coffeehouse.home.reservation.job.QueryAvailabilityJob.QueryAvailabilityJobFactory;
import org.coffeehouse.home.reservation.job.UpdateReservationJob.UpdateReservationJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
class ReservationController {

	private final ReservationRepository mRepository;

	private final ReservationResourceAssembler mAssembler;

	@Autowired
	private QueryAvailabilityJobFactory mQueryAvailabilityJobFactory;

	@Autowired
	private NewReservationJobFactory mReservationJobFactory;

	@Autowired
	private UpdateReservationJobFactory mUpdateReservationJobFactory;

	@Autowired
	private CancelReservationJobFactory mCancelReservationJobFactory;

	@Autowired
	private LocalDateHelper mLocalDateHelper;

	@Autowired
	@Qualifier("reservationUpdateExecutor")
	private ExecutorService updateExecutorService;

	@Autowired
	@Qualifier("reservationQueryExecutor")
	private ExecutorService queryExecutorService;

	ReservationController(ReservationRepository repository, ReservationResourceAssembler assembler) {
		this.mAssembler = assembler;
		this.mRepository = repository;
	}

	@GetMapping("/reservation/{id}")
	public Resource<Reservation> findById(@PathVariable String id) {
		try {
			log.info("Looking up reservation with id " + id);
			UUID uuid = UUID.fromString(id);
			Reservation reservation = mRepository.findById(uuid).orElseThrow(() -> new ReservationNotFoundException());
			return mAssembler.toResource(reservation);
		} catch (IllegalArgumentException exception) {
			log.error(exception.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Id, Id should be in the form if a UUID.",
					exception);
		}
	}

	@GetMapping("/reservation")
	public Resources<Resource<Reservation>> all() {
		log.info("Returning all reservations");
		List<Object> reservations = mRepository.findAll().stream().map(mAssembler::toResource)
				.collect(Collectors.toList());
		return new Resources(reservations, linkTo(methodOn(ReservationController.class).all()).withSelfRel());
	}

	@GetMapping("/reservation/availability/{startDate}/{endDate}")
	public Resources<List<LocalDate>> availability(@PathVariable("startDate") String startDate,
			@PathVariable("endDate") String endDate) {
		try {
			LocalDate mStartDate = LocalDate.parse(startDate);
			LocalDate mEndDate = LocalDate.parse(endDate);

			if (mEndDate.isBefore(mStartDate)) {
				throw new DateRangeException("End date cannot be before start date");
			}

			RESULT validation = mLocalDateHelper.areDatesValid(mStartDate, mEndDate);
			switch (validation) {
			case INVALID_START:
				throw new DateRangeException(
						"Invalid start date. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. ");
			case INVALID_END:
				throw new DateRangeException(
						"Invalid end date. The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. ");
			default:
				log.info("Querying Availability from StartDate: " + mStartDate + " to " + mEndDate);
				List<LocalDate> availableDates = queryExecutorService
						.submit(mQueryAvailabilityJobFactory.getJob(mStartDate, mEndDate)).get();
				return new Resources(availableDates,
						linkTo(methodOn(ReservationController.class).availability(startDate, endDate)).withSelfRel(),
						linkTo(methodOn(ReservationController.class).availability()).withRel("availabilities"));
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} catch (DateRangeException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (DateTimeParseException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dates must be in the form YYYY-MM-DD", e);
		}
	}

	@GetMapping("/reservation/availability")
	public Resources<List<LocalDate>> availability() {
		try {
			LocalDate mStartDate = LocalDate.now().plusDays(1);
			LocalDate mEndDate = LocalDate.now().plusMonths(1);

			log.info("Querying Availability from StartDate: " + mStartDate + " to " + mEndDate);

			List<LocalDate> availableDates = queryExecutorService
					.submit(mQueryAvailabilityJobFactory.getJob(mStartDate, mEndDate)).get();

			return new Resources(availableDates,
					linkTo(methodOn(ReservationController.class).availability()).withSelfRel());
		} catch (InterruptedException | ExecutionException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} catch (DateTimeParseException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dates must be in the form YYYY-MM-DD", e);
		}
	}

	@PostMapping("/reservation/reserve")
	public Resource<Reservation> reserve(@RequestBody Reservation newReservation) {
		try {
			log.info("Attempting to create a new reservation");
			Reservation reservation = updateExecutorService.submit(mReservationJobFactory.getJob(newReservation)).get();
			return mAssembler.toResource(reservation);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} catch (ExecutionException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
	}

	@PutMapping("/reservation/modify/{id}")
	public Resource<Reservation> modify(@RequestBody Reservation modReservation, @PathVariable String id) {
		try {
			log.info("Attempting to modify reservation with id " + id);
			UUID uuid = UUID.fromString(id);
			Reservation reservation = updateExecutorService
					.submit(mUpdateReservationJobFactory.getJob(uuid, modReservation)).get();
			return mAssembler.toResource(reservation);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} catch (IllegalArgumentException exception) {
			log.error(exception.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Id, Id should be in the form if a UUID.",
					exception);
		} catch (ExecutionException exception) {
			log.error(exception.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	@DeleteMapping("/reservation/cancel/{id}")
	ResponseEntity<?> cancel(@PathVariable String id) {
		try {
			log.info("Attempting to cancel reservation with id " + id);
			UUID uuid = UUID.fromString(id);
			updateExecutorService.submit(mCancelReservationJobFactory.getJob(uuid)).get();
			return ResponseEntity.noContent().build();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		} catch (IllegalArgumentException exception) {
			log.error(exception.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Id, Id should be in the form if a UUID.",
					exception);
		} catch (ExecutionException exception) {
			log.error(exception.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

}