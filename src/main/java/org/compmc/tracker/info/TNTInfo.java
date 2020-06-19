package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.level.Location;
import org.compmc.tracker.api.info.RangedInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class TNTInfo extends OwnerInfoBase implements RangedInfo {

  private final Location origin;

  public TNTInfo(TrackedPlayer owner, Location origin) {
    super(owner);
    this.origin = checkNotNull(origin);
  }

  @Override
  public Location getOrigin() {
    return origin;
  }
}
