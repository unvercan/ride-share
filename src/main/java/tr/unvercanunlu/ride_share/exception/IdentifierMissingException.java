package tr.unvercanunlu.ride_share.exception;

import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;
import tr.unvercanunlu.ride_share.core.entity.BaseEntity;

public class IdentifierMissingException extends RideSharingBaseException {

  @Getter
  private final Class<? extends BaseEntity<?>> entity;

  public IdentifierMissingException(Class<? extends BaseEntity<?>> entity) {
    super(ErrorMessage.IDENTIFIER_MISSING, entity.getSimpleName());
    this.entity = entity;
  }

}
