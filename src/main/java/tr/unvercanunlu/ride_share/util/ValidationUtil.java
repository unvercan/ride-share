package tr.unvercanunlu.ride_share.util;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {

  public static void checkIdNotNull(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("ID missing!");
    }
  }

}
