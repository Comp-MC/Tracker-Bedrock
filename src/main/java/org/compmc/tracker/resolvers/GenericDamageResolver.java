package org.compmc.tracker.resolvers;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.info.GenericDamageInfo;

public class GenericDamageResolver implements DamageResolver {

  @Override
  public @Nullable
  DamageInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    if (damager instanceof DamageInfo) {
      // If the damager block/entity resolved to a DamageInfo directly, return that
      return (DamageInfo) damager;
    } else {
      return new GenericDamageInfo(damageType, damager);
    }
  }
}
