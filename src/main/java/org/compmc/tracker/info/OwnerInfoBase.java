package org.compmc.tracker.info;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.player.TrackedPlayer;

public abstract class OwnerInfoBase implements OwnerInfo {

  private final @Nullable
  TrackedPlayer owner;

  public OwnerInfoBase(@Nullable TrackedPlayer owner) {
    this.owner = owner;
  }

  @Override
  public @Nullable
  TrackedPlayer getOwner() {
    return owner;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{owner=" + getOwner() + "}";
  }
}
