package org.compmc.tracker.api.info;

import cn.nukkit.level.Location;
import javax.annotation.Nullable;

public interface RangedInfo extends TrackerInfo {

  @Nullable
  Location getOrigin();
}
