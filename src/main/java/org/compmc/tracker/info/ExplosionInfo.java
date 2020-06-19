package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.level.Location;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.CauseInfo;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.RangedInfo;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class ExplosionInfo implements DamageInfo, RangedInfo, CauseInfo {

  private final PhysicalInfo explosive;

  public ExplosionInfo(PhysicalInfo explosive) {
    this.explosive = checkNotNull(explosive);
  }

  public PhysicalInfo getExplosive() {
    return explosive;
  }

  @Override
  public TrackerInfo getCause() {
    return getExplosive();
  }

  @Override
  public @Nullable
  Location getOrigin() {
    return explosive instanceof RangedInfo ? ((RangedInfo) explosive).getOrigin() : null;
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return explosive == null ? null : explosive.getOwner();
  }
}
