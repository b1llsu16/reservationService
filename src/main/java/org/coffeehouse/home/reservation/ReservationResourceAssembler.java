package org.coffeehouse.home.reservation;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.coffeehouse.home.reservation.data.Reservation;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
class ReservationResourceAssembler implements ResourceAssembler<Reservation, Resource<Reservation>> {

  @Override
  public Resource<Reservation> toResource(Reservation reservation) {

    return new Resource<>(reservation,
      linkTo(methodOn(ReservationController.class).findById(reservation.getId().toString())).withSelfRel(),
      linkTo(methodOn(ReservationController.class).all()).withRel("reservations"));
  }
}