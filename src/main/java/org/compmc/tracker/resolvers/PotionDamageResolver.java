package org.compmc.tracker.resolvers;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import javax.annotation.Nullable;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.PotionInfo;
import org.compmc.tracker.info.ProjectileInfo;

public class PotionDamageResolver implements DamageResolver {

  @Override
  public @Nullable
  PotionInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    if (damageType != DamageCause.MAGIC) {
      return null;
    }

    // If potion is already resolved (i.e. as a splash potion), leave it alone
    if (damager instanceof PotionInfo
        || damager instanceof ProjectileInfo
        && ((ProjectileInfo) damager).getProjectile() instanceof PotionInfo) {
      return null;
    }

    throw new RuntimeException("Could not determine effect type from damage event!");
  }
}
