package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.level.Location;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.PotionInfo;
import org.compmc.tracker.api.info.RangedInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class ProjectileInfo implements PhysicalInfo, DamageInfo, RangedInfo {

  private final PhysicalInfo projectile;
  private final @Nullable
  PhysicalInfo shooter;
  private final Location origin;
  private final @Nullable
  String customName;

  public ProjectileInfo(
      PhysicalInfo projectile,
      @Nullable PhysicalInfo shooter,
      Location origin,
      @Nullable String customName) {
    this.projectile = checkNotNull(projectile);
    this.shooter = shooter;
    this.origin = checkNotNull(origin);
    this.customName = customName;
  }

  public PhysicalInfo getProjectile() {
    return projectile;
  }

  public @Nullable
  PhysicalInfo getShooter() {
    return shooter;
  }

  @Override
  public Location getOrigin() {
    return this.origin;
  }

  @Override
  public @Nullable
  TrackedPlayer getOwner() {
    return shooter == null ? null : shooter.getOwner();
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }

  @Override
  public String getIdentifier() {
    return getProjectile().getIdentifier();
  }

  @Override
  public String getName() {
    if (customName != null) {
      return customName;
    } else if (getProjectile() instanceof PotionInfo) {
      // PotionInfo.getName returns a potion name,
      // which doesn't work outside a potion death message.
      return "Potion";
    } else {
      return getProjectile().getName();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{projectile="
        + getProjectile()
        + " origin="
        + getOrigin()
        + " shooter="
        + getShooter()
        + "}";
  }
}
