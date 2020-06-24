package org.compmc.tracker.trackers;

import cn.nukkit.Server;
import cn.nukkit.block.BlockLadder;
import cn.nukkit.block.BlockLava;
import cn.nukkit.block.BlockVine;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.utils.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.DamageResolver;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.FallInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.events.player.PlayerOnGroundEvent;
import org.compmc.tracker.events.player.PlayerSpleefEvent;
import org.compmc.tracker.info.FallState;
import org.compmc.tracker.info.GenericFallInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Tracks the state of falls caused by other players and resolves the damage caused by them.
 */
public class FallTracker implements Listener, DamageResolver {

  private final Map<TrackedPlayer, FallState> falls = new HashMap<>();

  private final TrackerSupervisor supervisor;
  private final Trackers tracker;
  private final Logger logger;

  public FallTracker(Trackers tracker, TrackerSupervisor supervisor) {
    this.tracker = tracker;
    this.supervisor = supervisor;
    this.logger = TrackerPlugin.INSTANCE.getLogger();
  }

  @Override
  public @Nullable
  FallInfo resolveDamage(
      EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
    FallState fall = getFall(victim);

    if (fall != null) {
      switch (damageType) {
        case VOID:
          fall.to = FallInfo.To.VOID;
          break;
        case FALL:
          fall.to = FallInfo.To.GROUND;
          break;
        case LAVA:
          fall.to = FallInfo.To.LAVA;
          break;

        case FIRE_TICK:
          if (fall.isInLava) {
            fall.to = FallInfo.To.LAVA;
          } else {
            return null;
          }
          break;

        default:
          return null;
      }

      return fall;
    } else {
      switch (damageType) {
        case FALL:
          return new GenericFallInfo(
              FallInfo.To.GROUND, victim.getLocation(), victim.fallDistance);
        case VOID:
          return new GenericFallInfo(
              FallInfo.To.VOID, victim.getLocation(), victim.fallDistance);
      }

      return null;
    }
  }

  @Nullable
  FallState getFall(Entity victim) {
    TrackedPlayer player = this.supervisor.getPlayer(victim);
    if (player == null) {
      return null;
    }

    FallState fall = falls.get(player);
    if (fall == null || !fall.isStarted || fall.isEnded) {
      return null;
    }

    return fall;
  }

  void endFall(FallState fall) {
    endFall(fall.victim);
  }

  void endFall(TrackedPlayer victim) {
    FallState fall = this.falls.remove(victim);
    if (fall != null) {
      fall.isEnded = true;
      logger.debug("Ended " + fall);
    }
  }

  void checkFallTimeout(final FallState fall) {
    long now = System.currentTimeMillis();
    if ((fall.isStarted && fall.isEndedSafely(now)) || (!fall.isStarted && fall.isExpired(now))) {

      endFall(fall);
    }
  }

  void scheduleCheckFallTimeout(final FallState fall, final int delay) {
    Server.getInstance().getScheduler()
        .scheduleDelayedTask(TrackerPlugin.INSTANCE,
            () -> {
              if (!fall.isEnded) {
                checkFallTimeout(fall);
              }
            },
            (delay + 1) * 20);
  }

  /**
   * Called whenever the player becomes "unsupported" to check if they were attacked recently enough
   * for the attack to be responsible for the fall
   */
  private void playerBecameUnsupported(FallState fall) {
    if (!fall.isStarted
        && !fall.isSupported()
        && System.currentTimeMillis() - fall.startTime <= FallState.MAX_KNOCKBACK_TICKS) {
      fall.isStarted = true;
      logger.debug("Started " + fall);
    }
  }

  /**
   * Called when a player is damaged in a way that could initiate a Fall, i.e. damage from another
   * entity that causes knockback
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onAttack(final EntityDamageEvent event) {
    // Filter out damage types that don't cause knockback
    switch (event.getCause()) {
      case ENTITY_ATTACK:
      case PROJECTILE:
      case BLOCK_EXPLOSION:
      case ENTITY_EXPLOSION:
      case MAGIC:
      case CUSTOM:
        break;

      default:
        return;
    }

    TrackedPlayer victim = this.supervisor.getPlayer(event.getEntity());
    if (victim == null) {
      return;
    }

    if (this.falls.containsKey(victim)) {
      // A new fall can't be initiated if the victim is already falling
      return;
    }

    Location loc = victim.getBase().getLocation();
    boolean isInLava = loc.getLevelBlock() instanceof BlockLava;
    boolean isClimbing =
        loc.getLevelBlock() instanceof BlockLadder || loc.getLevelBlock() instanceof BlockVine;
    boolean isSwimming = loc.getLevelBlock() instanceof BlockWater;

    DamageInfo cause = tracker.resolveDamage(event);

    // Note the victim's situation when the attack happened
    FallInfo.From from;
    if (isClimbing) {
      from = FallInfo.From.LADDER;
    } else if (isSwimming) {
      from = FallInfo.From.WATER;
    } else {
      from = FallInfo.From.GROUND;
    }

    FallState fall = new FallState(victim, from, cause);
    this.falls.put(victim, fall);

    fall.isClimbing = isClimbing;
    fall.isSwimming = isSwimming;
    fall.isInLava = isInLava;

    // If the victim is already in the air, immediately confirm that they are falling.
    // Otherwise, the fall will be confirmed when they leave the ground, if it happens
    // within the time window.
    fall.isStarted = !fall.isSupported();

    if (!fall.isStarted) {
      this.scheduleCheckFallTimeout(fall, FallState.MAX_KNOCKBACK_TICKS);
    }

    logger.debug("Attacked " + fall);
  }

  /**
   * Called when a player moves in a way that could affect their fall i.e. landing on a ladder or in
   * liquid
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(final PlayerMoveEvent event) {
    TrackedPlayer player = this.supervisor.getPlayer(event.getPlayer());
    if (player == null) {
      return;
    }

    FallState fall = this.falls.get(player);
    if (fall != null) {
      boolean isInLava = event.getTo().getLevelBlock() instanceof BlockLava;
      boolean isClimbing = event.getTo().getLevelBlock() instanceof BlockLadder || event.getTo()
          .getLevelBlock() instanceof BlockVine;
      boolean isSwimming = event.getTo().getLevelBlock() instanceof BlockWater;
      boolean becameUnsupported = false;
      long now = System.currentTimeMillis();

      if (isClimbing != fall.isClimbing) {
        if ((fall.isClimbing = isClimbing)) {
          // Player moved onto a ladder, cancel the fall if they are still on it after
          // MAX_CLIMBING_TIME
          fall.climbingTick = now;
          this.scheduleCheckFallTimeout(fall, FallState.MAX_CLIMBING_TICKS + 1);
        } else {
          becameUnsupported = true;
        }
      }

      if (isSwimming != fall.isSwimming) {
        if ((fall.isSwimming = isSwimming)) {
          // TrackedPlayer moved into water, cancel the fall if they are still in it after
          // MAX_SWIMMING_TIME
          fall.swimmingTick = now;
          this.scheduleCheckFallTimeout(fall, FallState.MAX_SWIMMING_TICKS + 1);
        } else {
          becameUnsupported = true;
        }
      }

      if (becameUnsupported) {
        // TrackedPlayer moved out of water or off a ladder, check if it was caused by the attack
        this.playerBecameUnsupported(fall);
      }

      if (isInLava != fall.isInLava) {
        if ((fall.isInLava = isInLava)) {
          fall.inLavaTick = now;
        } else {
          // Because players continue to "fall" as long as they are in lava, moving out of lava
          // can immediately finish their fall
          this.checkFallTimeout(fall);
        }
      }
    }
  }

  /**
   * Called when the player touches or leaves the ground
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerOnGroundChanged(final PlayerOnGroundEvent event) {
    TrackedPlayer player = this.supervisor.getPlayer(event.getPlayer());
    if (player == null) {
      return;
    }

    FallState fall = this.falls.get(player);
    if (fall != null) {
      if (event.getOnGround()) {
        // Falling player landed on the ground, cancel the fall if they are still there after
        // MAX_ON_GROUND_TIME
        fall.onGroundTick = System.currentTimeMillis();
        fall.groundTouchCount++;
        this.scheduleCheckFallTimeout(fall, FallState.MAX_ON_GROUND_TICKS + 1);
      } else {
        // Falling player left the ground, check if it was caused by the attack
        this.playerBecameUnsupported(fall);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerSpleef(final PlayerSpleefEvent event) {
    TrackedPlayer victim = event.getVictim();
    FallState fall = this.falls.get(victim);
    if (fall == null || !fall.isStarted) {
      if (fall != null) {
        // End the existing fall and replace it with the spleef
        endFall(fall);
      }

      fall = new FallState(victim, FallInfo.From.GROUND, event.getSpleefInfo());
      fall.isStarted = true;

      Location loc = victim.getBase().getLocation();
      fall.isInLava = loc.getLevelBlock() instanceof BlockLava;
      fall.isClimbing =
          loc.getLevelBlock() instanceof BlockLadder || loc.getLevelBlock() instanceof BlockVine;
      fall.isSwimming = loc.getLevelBlock() instanceof BlockWater;

      this.falls.put(victim, fall);

      logger.debug("Spleefed " + fall);
    }
  }

  // NOTE: This must be called after anything that tries to resolve the death
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerDeath(final PlayerDeathEvent event) {
    TrackedPlayer player = this.supervisor.getPlayer(event.getEntity());
    if (player != null) {
      endFall(player);
    }
  }

//  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//  public void onPlayerDespawn(final PlayerRespawnEvent event) {
//    endFall(event.getPlayer());
//  }
}
