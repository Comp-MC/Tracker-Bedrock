package org.compmc.tracker.trackers;

import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLava;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityCombustByBlockEvent;
import cn.nukkit.event.entity.EntityCombustByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.events.world.BlockChangeByPlayerEvent;
import org.compmc.tracker.events.world.BlockChangeEvent;
import org.compmc.tracker.info.BlockInfo;
import org.compmc.tracker.info.EntityInfo;
import org.compmc.tracker.info.FireInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * - Updates the state of owned fire and lava blocks from events - Tracks burning entities that were
 * ignited by players, directly or indirectly - Resolves fire tick damage to those entities
 */
public class FireTracker extends AbstractTracker<FireInfo> implements DamageResolver {

  // An entity can be owned by one player but ignited by another, so we need an independent map for
  // burning
  private final Map<Entity, FireInfo> burningEntities = new WeakHashMap<>();

  public FireTracker(Trackers trackers, TrackerSupervisor supervisor) {
    super(FireInfo.class, trackers, supervisor);
  }

  @Override
  public @Nullable
  FireInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    switch (damageType) {
      case FIRE_TICK:
        FireInfo info = resolveBurning(victim);
        if (info != null) {
          return info;
        }
        // fall through

      case FIRE:
      case LAVA:
        return new FireInfo(damager);
    }
    return null;
  }

  public @Nullable
  FireInfo resolveBurning(Entity entity) {
    if (!entity.isOnFire()) {
      this.burningEntities.remove(entity);
      return null;
    }
    return burningEntities.get(entity);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockTransform(BlockChangeEvent event) {
    boolean wasLava = event.getFrom() instanceof BlockLava;
    boolean isLava = event.getTo() instanceof BlockLava;

    if (event.changedFrom(BlockID.FIRE) || (wasLava && !isLava)) {
      blocks().clearBlock(event.getBlock());
    }
    if (event instanceof BlockChangeByPlayerEvent
        && (event.changedTo(BlockID.FIRE) || (!wasLava && isLava))) {
      TrackedPlayer placer = ((BlockChangeByPlayerEvent) event).getPlayer();
      blocks()
          .trackBlockState(
              event.getTo(), new FireInfo(new BlockInfo(event.getTo(), placer)));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityIgnite(EntityCombustByBlockEvent event) {
    if (event.getDuration() == 0) {
      return;
    }

    TrackedPlayer owner = blocks().getOwner(event.getCombuster());
    if (owner != null) {
      burningEntities.put(
          event.getEntity(), new FireInfo(blocks().resolveBlock(event.getCombuster())));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityIgnite(EntityCombustByEntityEvent event) {
    if (event.getDuration() == 0) {
      return;
    }

    FireInfo info = resolveBurning(event.getCombuster());
    if (info != null) {
      // First, try to resolve the player who ignited the combuster
      info = new FireInfo(new EntityInfo(event.getCombuster(), info.getOwner()));
    } else {
      // If an igniter is not found, fall back to the owner of the entity
      info = new FireInfo(entities().resolveEntity(event.getCombuster()));
    }

    burningEntities.put(event.getEntity(), info);
  }
}
