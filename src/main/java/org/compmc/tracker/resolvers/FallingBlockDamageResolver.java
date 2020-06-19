package org.compmc.tracker.resolvers;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.info.FallingBlockInfo;

public class FallingBlockDamageResolver implements DamageResolver {

  @Override
  public @Nullable
  FallingBlockInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    if (false // TODO: Nukkit doesn't have a falling block cause?
        && damager instanceof FallingBlockInfo) {
      return (FallingBlockInfo) damager;
    }
    return null;
  }
}
