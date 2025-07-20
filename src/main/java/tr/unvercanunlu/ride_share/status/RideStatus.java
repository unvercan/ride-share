package tr.unvercanunlu.ride_share.status;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public enum RideStatus {

  REQUESTED('R'),
  ACCEPTED('A'),
  APPROVED('P'),
  STARTED('S'),
  COMPLETED('C'),
  CANCELED('X'),
  EXPIRED('E');

  @Getter
  private final char code;

  private static final Map<RideStatus, Set<RideStatus>> TRANSITIONS;

  static {
    Map<RideStatus, Set<RideStatus>> map = new EnumMap<>(RideStatus.class);

    map.put(REQUESTED, EnumSet.of(ACCEPTED, CANCELED, EXPIRED));
    map.put(ACCEPTED, EnumSet.of(APPROVED, REQUESTED, CANCELED));
    map.put(APPROVED, EnumSet.of(STARTED, CANCELED));
    map.put(STARTED, EnumSet.of(COMPLETED, CANCELED));
    map.put(COMPLETED, Collections.emptySet());
    map.put(CANCELED, Collections.emptySet());
    map.put(EXPIRED, Collections.emptySet());

    TRANSITIONS = Collections.unmodifiableMap(map);
  }

  public boolean canTransitionTo(RideStatus next) {
    if ((next != null) && TRANSITIONS.containsKey(this)) {
      return TRANSITIONS.get(this).contains(next);
    }

    return false;
  }

  public Set<RideStatus> getAllowedTransitions() {
    return Collections.unmodifiableSet(TRANSITIONS.get(this));
  }

}
