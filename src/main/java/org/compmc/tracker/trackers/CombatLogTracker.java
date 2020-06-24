package org.compmc.tracker.trackers;

import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLava;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByBlockEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.potion.Effect;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Predicts the death of players who disconnect while participating, and simulates the damage and
 * death events that would have been fired if they had stayed in the game.
 *
 * <p>Also prevents team switching while in imminent danger..
 */
public class CombatLogTracker implements Listener {

  // Logout within this time since last damage is considered combat log
  private static final Duration RECENT_DAMAGE_THRESHOLD = Duration.ofSeconds(3);

  // Maximum height player can fall without taking damage
  private static final double SAFE_FALL_DISTANCE = 2;

  // Minimum water required to stop the player's fall
  private static final int BREAK_FALL_WATER_DEPTH = 3;
  // A simple way to tag an event as a combat log, hacky but it works
  private static @Nullable
  PlayerDeathEvent currentDeathEvent;
  private final TrackerSupervisor supervisor;
  private Map<Player, Damage> recentDamage = new HashMap<>();

  public CombatLogTracker(TrackerSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  private static boolean hasFireResistance(EntityLiving entity) {
    for (Effect effect : entity.getEffects().values()) {
      if (Effect.FIRE_RESISTANCE == effect.getId()) {
        return true;
      }
    }
    return false;
  }

  private static double getResistanceFactor(EntityLiving entity) {
    int amplifier = 0;
    for (Effect effect : entity.getEffects().values()) {
      if (Effect.DAMAGE_RESISTANCE == effect.getId()
          && effect.getAmplifier() > amplifier) {
        amplifier = effect.getAmplifier();
      }
    }
    return 1d - (amplifier / 5d);
  }

  public static boolean isCombatLog(PlayerDeathEvent event) {
    return event == currentDeathEvent;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerDamage(EntityDamageEvent event) {
    if (event.getDamage() <= 0) {
      return;
    }

    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();

    if (player.gamemode == Player.CREATIVE) {
      return;
    }

    if (!player.isAlive()) {
      return;
    }

    if (getResistanceFactor(player) <= 0) {
      return;
    }

    switch (event.getCause()) {
      case ENTITY_EXPLOSION:
      case BLOCK_EXPLOSION:
      case CUSTOM:
      case FALL:
      case LIGHTNING:
      case SUICIDE:
        return; // Skip damage causes that are not particularly likely to be followed by more damage

      case FIRE:
      case FIRE_TICK:
      case LAVA:
        if (hasFireResistance(player)) {
          return;
        }
        break;
    }

    // Record the player's damage with a timestamp
    this.recentDamage.put(player, new Damage(Instant.now(), event));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerDeath(PlayerDeathEvent event) {
    // Clear last damage when a player dies
    this.recentDamage.remove(event.getEntity());
  }

  // This must be called BEFORE the listener that removes the player from the round
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onQuit(PlayerQuitEvent event) {
    TrackerSupervisor supervisor = this.supervisor;
    if (!supervisor.inCombat()) {
      return;
    }

    TrackedPlayer player = supervisor.getPlayer(event.getPlayer());
    if (player == null || !player.isParticipating()) {
      return;
    }

    ImminentDeath imminentDeath = this.getImminentDeath(player.getBase());
    if (imminentDeath == null) {
      return;
    }

    if (!imminentDeath.alreadyDamaged) {
      // Simulate the damage event that would have killed them,
      // allowing the tracker to figure out the cause of death
      EntityDamageEvent damageEvent;
      if (imminentDeath.blockDamager != null) {
        damageEvent =
            new EntityDamageByBlockEvent(
                imminentDeath.blockDamager,
                player.getBase(),
                imminentDeath.cause,
                player.getBase().getHealth());
      } else {
        damageEvent =
            new EntityDamageEvent(
                player.getBase(), imminentDeath.cause, player.getBase().getHealth());
      }
      Server.getInstance().getPluginManager().callEvent(damageEvent);

      // If the damage event was cancelled, don't simulate the kill
      if (damageEvent.isCancelled()) {
        return;
      }

      player.getBase().setLastDamageCause(damageEvent);
    }

    // Simulate the player's death. The tracker will assume the death was caused by the
    // last damage event, which was either a real one or the fake one we generated above.
    ArrayList<Item> drops = new ArrayList<>();
    for (Item stack : player.getBase().getInventory().getContents().values()) {
      if (stack != null && stack.getId() != Item.AIR) {
        drops.add(stack);
      }
    }
    for (Item stack : player.getBase().getInventory().getArmorContents()) {
      if (stack != null && stack.getId() != Item.AIR) {
        drops.add(stack);
      }
    }

    try {
      currentDeathEvent =
          new PlayerDeathEvent(
              player.getBase(),
              drops.toArray(new Item[]{}),
              player.getBase().getDisplayName() + " logged out to avoid death",
              0);
      Server.getInstance().getPluginManager().callEvent(currentDeathEvent);
    } finally {
      currentDeathEvent = null;
    }
  }

//  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//  public void onParticipationStop(PlayerParticipationStopEvent event) {
//    if (event.getMatch().isRunning()
//        && this.getImminentDeath(event.getPlayer().getBase()) != null) {
//      event.cancel(new PersonalizedTranslatable("leave.err.combatLog"));
//      event.setCancelled(true);
//    }
//  }

  /**
   * Get the cause of the player's imminent death, or null if they are not about to die NOTE: not
   * idempotent, has the side effect of clearing the recentDamage cache
   */
  public @Nullable
  ImminentDeath getImminentDeath(Player player) {
    // If the player is already dead or in creative mode, we don't care
    if (!player.isAlive() || player.gamemode == Player.CREATIVE) {
      return null;
    }

    // If the player was on the ground, or is flying, or is able to fly, they are fine
    if (!(player.isOnGround() || player.getAdventureSettings().get(Type.FLYING) || player
        .getAdventureSettings().get(
            Type.ALLOW_FLIGHT))) {
      // If the player is falling, detect an imminent falling death
      double fallDistance = player.fallDistance;
      Block landingBlock = null;
      int waterDepth = 0;
      Location location = player.getLocation();

      if (location.getY() > 256) {
        // If player is above Y 256, assume they fell at least to there
        fallDistance += location.getY() - 256;
        location.y = 256;
      }

      // Search the blocks directly beneath the player until we find what they would have landed on
      Block block = null;
      for (; location.getY() >= 0; location.add(0, -1, 0)) {
        block = location.getLevelBlock();
        if (block != null) {
          landingBlock = block;

          if (landingBlock instanceof BlockWater) {
            // If the player falls through water, reset fall distance and inc the water depth
            fallDistance = -1;
            waterDepth += 1;

            // Break if they have fallen through enough water to stop falling
            if (waterDepth >= BREAK_FALL_WATER_DEPTH) {
              break;
            }
          } else {
            // If the block is not water, reset the water depth
            waterDepth = 0;

            if (landingBlock.isSolid() || landingBlock instanceof BlockLava) {
              // Break if the player hits a solid block or lava
              break;
            } else if (landingBlock.getId() == BlockID.COBWEB) {
              // If they hit web, reset their fall distance, but assume they keep falling
              fallDistance = -1;
            }
          }
        }

        fallDistance += 1;
      }

      double resistanceFactor = getResistanceFactor(player);
      boolean fireResistance = hasFireResistance(player);

      // Now decide if the landing would have killed them
      if (location.getFloorY() < 0) {
        // The player would have fallen into the void
        return new ImminentDeath(EntityDamageEvent.DamageCause.VOID, location, null, false);
      } else if (landingBlock != null) {
        if (landingBlock.isSolid()
            && player.getHealth() <= resistanceFactor * (fallDistance - SAFE_FALL_DISTANCE)) {
          // The player would have landed on a solid block and taken enough fall damage to kill them
          return new ImminentDeath(
              EntityDamageEvent.DamageCause.FALL,
              landingBlock.getLocation().add(0, 0.5, 0),
              null,
              false);
        } else if (landingBlock instanceof BlockLava
            && resistanceFactor > 0
            && !fireResistance) {
          // The player would have landed in lava, and we give the lava the benefit of the doubt
          return new ImminentDeath(
              EntityDamageEvent.DamageCause.LAVA, landingBlock.getLocation(), landingBlock, false);
        }
      }
    }

    // If we didn't predict a falling death, detect combat log due to recent damage
    Damage damage = this.recentDamage.remove(player);
    if (damage != null && damage.time.plus(RECENT_DAMAGE_THRESHOLD).isAfter(Instant.now())) {
      // TrackedPlayer logged out too soon after taking damage
      return new ImminentDeath(damage.event.getCause(), player.getLocation(), null, true);
    }

    return null;
  }

  private static class Damage {

    public final Instant time;
    public final EntityDamageEvent event;

    private Damage(Instant time, EntityDamageEvent event) {
      this.time = time;
      this.event = event;
    }
  }

  private static class ImminentDeath {

    public final EntityDamageEvent.DamageCause cause; // what will cause the death
    public final Location deathLocation;
    public final Block blockDamager;
    public final boolean alreadyDamaged; // if the player has already been damaged by this cause

    private ImminentDeath(
        EntityDamageEvent.DamageCause cause,
        Location deathLocation,
        @Nullable Block blockDamager,
        boolean damaged) {
      this.cause = cause;
      this.deathLocation = deathLocation;
      this.blockDamager = blockDamager;
      this.alreadyDamaged = damaged;
    }
  }
}
