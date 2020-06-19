package org.compmc.tracker.api;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;

public interface DamageResolver {

  @Nullable
  DamageInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager);
}
