package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.NotFoundException;

public interface PassengerService {

  Passenger register(RegisterPassengerDto request);

  Passenger getPassenger(UUID passengerId) throws NotFoundException;

}
