package tr.unvercanunlu.ride_share.exception;

import java.util.UUID;
import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;
import tr.unvercanunlu.ride_share.core.BaseEntity;

public class NotFoundException extends RideSharingBaseException {

  @Getter
  private final UUID id;

  @Getter
  private final Class<? extends BaseEntity<?>> entity;

  public NotFoundException(Class<? extends BaseEntity<?>> entity, UUID id) {
    super(ErrorMessage.NOT_FOUND, entity.getSimpleName(), id);

    this.id = id;
    this.entity = entity;
  }

}
