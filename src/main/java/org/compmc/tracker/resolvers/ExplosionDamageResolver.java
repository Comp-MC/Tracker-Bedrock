package org.compmc.tracker.resolvers;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.info.ExplosionInfo;

public class ExplosionDamageResolver implements DamageResolver {

  @Override
  public @Nullable
  ExplosionInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    switch (damageType) {
      case ENTITY_EXPLOSION:
      case BLOCK_EXPLOSION:
        // Bukkit fires block explosion events with a null damager in rare situations
        return damager == null ? null : new ExplosionInfo(damager);

      default:
        return null;
    }
  }
}
