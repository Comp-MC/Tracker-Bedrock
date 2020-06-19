package org.compmc.tracker.info;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class AnvilInfo extends OwnerInfoBase implements DamageInfo {

  public AnvilInfo(TrackedPlayer owner) {
    super(owner);
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }
}
