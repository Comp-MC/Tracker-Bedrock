package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.CauseInfo;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class SpleefInfo implements DamageInfo, CauseInfo {

  private final DamageInfo breaker;
  private final long time;

  public SpleefInfo(DamageInfo breaker, long time) {
    this.breaker = checkNotNull(breaker);
    this.time = checkNotNull(time);
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getBreaker().getAttacker();
  }

  @Override
  public DamageInfo getCause() {
    return breaker;
  }

  public DamageInfo getBreaker() {
    return breaker;
  }

  public long getTime() {
    return time;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{time=" + getTime() + " breaker=" + getBreaker() + "}";
  }
}
