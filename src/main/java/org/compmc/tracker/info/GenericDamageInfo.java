package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.event.entity.EntityDamageEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.CauseInfo;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class GenericDamageInfo implements DamageInfo, CauseInfo {

  private final @Nullable
  PhysicalInfo damager;
  private final EntityDamageEvent.DamageCause damageType;

  public GenericDamageInfo(
      EntityDamageEvent.DamageCause damageType, @Nullable PhysicalInfo damager) {
    this.damageType = checkNotNull(damageType);
    this.damager = damager;
  }

  public GenericDamageInfo(EntityDamageEvent.DamageCause damageType) {
    this(damageType, null);
  }

  public @Nullable
  PhysicalInfo getDamager() {
    return damager;
  }

  @Override
  public @Nullable
  PhysicalInfo getCause() {
    return getDamager();
  }

  public EntityDamageEvent.DamageCause getDamageType() {
    return damageType;
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return damager == null ? null : damager.getOwner();
  }
}
