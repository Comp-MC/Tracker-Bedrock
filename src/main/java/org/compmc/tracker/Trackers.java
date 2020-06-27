package org.compmc.tracker;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Location;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.RangedInfo;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.info.NullDamageInfo;
import org.compmc.tracker.listeners.BlockChangeListener;
import org.compmc.tracker.listeners.EntityChangeListener;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.resolvers.ExplosionDamageResolver;
import org.compmc.tracker.resolvers.FallingBlockDamageResolver;
import org.compmc.tracker.resolvers.GenericDamageResolver;
import org.compmc.tracker.resolvers.PotionDamageResolver;
import org.compmc.tracker.supervisor.TrackerSupervisor;
import org.compmc.tracker.trackers.AnvilTracker;
import org.compmc.tracker.trackers.BlockTracker;
import org.compmc.tracker.trackers.CombatLogTracker;
import org.compmc.tracker.trackers.DeathTracker;
import org.compmc.tracker.trackers.DispenserTracker;
import org.compmc.tracker.trackers.EntityTracker;
import org.compmc.tracker.trackers.FallTracker;
import org.compmc.tracker.trackers.FireTracker;
import org.compmc.tracker.trackers.ProjectileTracker;
import org.compmc.tracker.trackers.SpleefTracker;
import org.compmc.tracker.trackers.TNTTracker;

public final class Trackers {

  private final List<Listener> listeners = Lists.newArrayList();
  private final Set<DamageResolver> damageResolvers = new LinkedHashSet<>();

  private final TrackerSupervisor supervisor;
  private final EntityTracker entityTracker;
  private final BlockTracker blockTracker;
  private final FallTracker fallTracker;
  private final FireTracker fireTracker;

  public Trackers(TrackerSupervisor supervisor) {
    this.supervisor = supervisor;
    entityTracker = new EntityTracker(supervisor);
    blockTracker = new BlockTracker(supervisor);
    fallTracker = new FallTracker(this, supervisor);
    fireTracker = new FireTracker(this, supervisor);

    listeners.addAll(
        Arrays.asList(
            entityTracker, blockTracker, fallTracker, fireTracker,
            new DispenserTracker(this, supervisor),
            new TNTTracker(this, supervisor),
            new SpleefTracker(this, supervisor),
            new ProjectileTracker(this, supervisor),
            new AnvilTracker(this, supervisor),
            new CombatLogTracker(supervisor),
            new DeathTracker(this, supervisor),

            new BlockChangeListener(supervisor),
            new EntityChangeListener()
        )
    );

    // Damage resolvers - order is important!
    damageResolvers.add(fallTracker);
    damageResolvers.add(fireTracker);
    damageResolvers.add(new PotionDamageResolver());
    damageResolvers.add(new ExplosionDamageResolver());
    damageResolvers.add(new FallingBlockDamageResolver());
    damageResolvers.add(new GenericDamageResolver());
  }

  public static double distanceFromRanged(RangedInfo rangedInfo, @Nullable Location deathLocation) {
    if (rangedInfo.getOrigin() == null || deathLocation == null) {
      return Double.NaN;
    }

    // When players fall in the void, use y=0 as their death location
    if (deathLocation.getY() < 0) {
      deathLocation = deathLocation.clone();
      deathLocation.y = 0;
    }
    return deathLocation.distance(rangedInfo.getOrigin());
  }

  public void load() {
    listeners.forEach(this::registerListener);
  }

  public void unload() {
    listeners.forEach(this::unregisterListener);
  }

  private void registerListener(Listener listener) {
    Server.getInstance().getPluginManager().registerEvents(listener, TrackerPlugin.INSTANCE);
  }

  private void unregisterListener(Listener listener) {
    HandlerList.unregisterAll(listener);
  }

  public EntityTracker getEntityTracker() {
    return entityTracker;
  }

  public BlockTracker getBlockTracker() {
    return blockTracker;
  }

  public DamageInfo resolveDamage(EntityDamageEvent damageEvent) {
    if (damageEvent instanceof EntityDamageByEntityEvent) {
      return resolveDamage((EntityDamageByEntityEvent) damageEvent);
    } else if (damageEvent instanceof EntityDamageByBlockEvent) {
      return resolveDamage((EntityDamageByBlockEvent) damageEvent);
    } else {
      return resolveDamage(damageEvent.getCause(), damageEvent.getEntity());
    }
  }

  public DamageInfo resolveDamage(EntityDamageByEntityEvent damageEvent) {
    return resolveDamage(damageEvent.getCause(), damageEvent.getEntity(), damageEvent.getDamager());
  }

  public DamageInfo resolveDamage(EntityDamageByBlockEvent damageEvent) {
    return resolveDamage(damageEvent.getCause(), damageEvent.getEntity(), damageEvent.getDamager());
  }

  public DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim) {
    return resolveDamage(damageType, victim, (PhysicalInfo) null);
  }

  public DamageInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Block damager) {
    if (damager == null) {
      return resolveDamage(damageType, victim);
    }
    return resolveDamage(damageType, victim, blockTracker.resolveBlock(damager));
  }

  public DamageInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Entity damager) {
    if (damager == null) {
      return resolveDamage(damageType, victim);
    }
    return resolveDamage(damageType, victim, entityTracker.resolveEntity(damager));
  }

  private DamageInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    // Filter out observers immediately
    if (supervisor.getPlayer(victim) == null) {
      return new NullDamageInfo();
    }

    for (DamageResolver resolver : damageResolvers) {
      DamageInfo resolvedInfo = resolver.resolveDamage(damageType, victim, damager);
      if (resolvedInfo != null) {
        return resolvedInfo;
      }
    }

    // This should never happen
    return new NullDamageInfo();
  }

  public TrackerInfo resolveInfo(Entity entity) {
    return entityTracker.resolveInfo(entity);
  }

  public @Nullable
  <T extends TrackerInfo> T resolveInfo(Entity entity, Class<T> infoType) {
    return entityTracker.resolveInfo(entity, infoType);
  }

  public @Nullable
  PhysicalInfo resolveShooter(Entity source) {
    return entityTracker.resolveEntity(source);
  }

  /**
   * Use every available means to determine the owner of the given {@link Entity}
   */
  public @Nullable
  TrackedPlayer getOwner(Entity entity) {
    return entityTracker.getOwner(entity);
  }

  public TrackerInfo resolveInfo(Block block) {
    return blockTracker.resolveInfo(block);
  }

  /**
   * Use every available means to determine the owner of the given {@link Block}
   */
  public @Nullable
  TrackedPlayer getOwner(Block block) {
    return blockTracker.getOwner(block);
  }
}
