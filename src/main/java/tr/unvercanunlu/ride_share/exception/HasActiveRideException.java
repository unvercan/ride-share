package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;
import tr.unvercanunlu.ride_share.entity.Person;

public class HasActiveRideException extends RideSharingBaseException {

  @Getter
  private final UUID id;

  @Getter
  private final Class<? extends Person> person;

  public HasActiveRideException(Class<? extends Person> person, UUID id) {
    super(ErrorMessage.HAS_ACTIVE_RIDE, person.getSimpleName(), id);
    this.id = id;
    this.person = person;
  }

}
