package org.compmc.tracker.api.info;

import javax.annotation.Nullable;
import org.compmc.tracker.player.TrackedPlayer;

public interface OwnerInfo extends TrackerInfo {

  @Nullable
  TrackedPlayer getOwner();
}
