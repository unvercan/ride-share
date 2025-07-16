package tr.unvercanunlu.ride_share.exception;

import lombok.Getter;
import tr.unvercanunlu.ride_share.config.ErrorMessage;
import tr.unvercanunlu.ride_share.core.BaseEntity;

public class IdentifierMissingException extends RideSharingException {

  @Getter
  private final String type;

  public IdentifierMissingException(Class<? extends BaseEntity<?>> entityClass) {
    super(ErrorMessage.IDENTIFIER_MISSING, entityClass.getName());

    this.type = entityClass.getName();
  }

}
