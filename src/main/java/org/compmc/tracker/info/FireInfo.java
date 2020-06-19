package org.compmc.tracker.info;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.CauseInfo;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class FireInfo implements OwnerInfo, CauseInfo, DamageInfo {

  private final @Nullable
  PhysicalInfo igniter;

  public FireInfo(@Nullable PhysicalInfo igniter) {
    this.igniter = igniter;
  }

  public FireInfo() {
    this(null);
  }

  public @Nullable
  PhysicalInfo getIgniter() {
    return igniter;
  }

  @Override
  public PhysicalInfo getCause() {
    return getIgniter();
  }

  @Override
  public @Nullable
  TrackedPlayer getOwner() {
    return igniter == null ? null : igniter.getOwner();
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{igniter=" + igniter + "}";
  }
}
