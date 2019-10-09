package org.coffeehouse.home.reservation.data;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Reservation {

	private @Id UUID id;
	private String name;
	private String email;
	private LocalDate startDate;
	private LocalDate endDate;

	public Reservation() {
		this.id = UUID.randomUUID();
	};
	
	public Reservation(String name, String email, LocalDate startDate, LocalDate endDate) {
		this.name = name;
		this.email = email;
		this.startDate = startDate;
		this.endDate = endDate;
		this.id = UUID.randomUUID();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

}