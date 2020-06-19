package org.compmc.tracker.api.info;

import javax.annotation.Nullable;
import org.compmc.tracker.player.TrackedPlayer;

public interface DamageInfo extends TrackerInfo {

  @Nullable
  TrackedPlayer getAttacker();
}
