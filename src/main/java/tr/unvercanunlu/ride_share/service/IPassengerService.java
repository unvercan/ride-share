package tr.unvercanunlu.ride_share.service;

import java.util.UUID;
import tr.unvercanunlu.ride_share.dto.request.RegisterPassengerDto;
import tr.unvercanunlu.ride_share.entity.Passenger;
import tr.unvercanunlu.ride_share.exception.PassengerNotFoundException;

public interface IPassengerService {

  Passenger registerPassenger(RegisterPassengerDto request);

  Passenger getPassengerDetail(UUID passengerId) throws PassengerNotFoundException;

}
