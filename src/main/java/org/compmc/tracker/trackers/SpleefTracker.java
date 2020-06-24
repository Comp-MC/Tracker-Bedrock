package org.compmc.tracker.trackers;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.level.Location;
import java.util.HashMap;
import java.util.Map;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.events.player.PlayerOnGroundEvent;
import org.compmc.tracker.events.player.PlayerSpleefEvent;
import org.compmc.tracker.events.world.BlockChangeByPlayerEvent;
import org.compmc.tracker.info.ExplosionInfo;
import org.compmc.tracker.info.PlayerInfo;
import org.compmc.tracker.info.SpleefInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Tracks blocks broken by players and fires a {@link PlayerSpleefEvent} when it appears to cause a
 * player to leave the ground.
 */
public class SpleefTracker implements Listener {

  // A player must leave the ground within this many ticks of a block being broken
  // under them for the fall to be caused by a spleef from that block
  public static final int MAX_SPLEEF_TICKS = 20;
  private static final float PLAYER_WIDTH = 0.6f;
  private static final float PLAYER_RADIUS = PLAYER_WIDTH / 2.0f;
  private final TrackerSupervisor supervisor;
  private final Trackers trackers;
  private final Map<Block, SpleefInfo> brokenBlocks = new HashMap<>();

  public SpleefTracker(Trackers trackers, TrackerSupervisor supervisor) {
    this.trackers = trackers;
    this.supervisor = supervisor;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(final BlockChangeByPlayerEvent event) {
    if (!event.isToAir()) {
      return;
    }
    if (!event.getFrom().isSolid()) {
      return;
    }

    final Block block = event.getBlock();
    DamageInfo breaker = null;

    if (event.getCause() instanceof EntityExplodeEvent) {
      PhysicalInfo explosive =
          trackers.resolveInfo(
              ((EntityExplodeEvent) event.getCause()).getEntity(), PhysicalInfo.class);
      if (explosive != null) {
        breaker = new ExplosionInfo(explosive);
      }
    }

    if (breaker == null) {
      breaker = new PlayerInfo(event.getPlayer());
    }

    final SpleefInfo info = new SpleefInfo(breaker, System.currentTimeMillis());
    brokenBlocks.put(block, info);

    Server.getInstance().getScheduler()
        .scheduleDelayedTask(TrackerPlugin.INSTANCE,
            () -> {
              // Only remove the BrokenBlock if it's the same one we added. It may have been
              // replaced since then.
              if (info == brokenBlocks.get(block)) {
                brokenBlocks.remove(block);
              }
            },
            (MAX_SPLEEF_TICKS + 1) * 20);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerOnGroundChanged(final PlayerOnGroundEvent event) {
    TrackedPlayer player = this.supervisor.getPlayer(event.getPlayer());
    if (player == null) {
      return;
    }

    Block block = this.lastBlockBrokenUnderPlayer(player);
    if (block != null) {
      SpleefInfo info = brokenBlocks.get(block);
      if (System.currentTimeMillis() - info.getTime() <= MAX_SPLEEF_TICKS) {
        Server.getInstance().getPluginManager()
            .callEvent(new PlayerSpleefEvent(player, block, info));
      }
    }
  }

  public Block lastBlockBrokenUnderPlayer(TrackedPlayer player) {
    Location playerLocation = player.getBase().getLocation();

    int y = (int) Math.floor(playerLocation.getY() - 0.1);

    int x1 = (int) Math.floor(playerLocation.getX() - PLAYER_RADIUS);
    int z1 = (int) Math.floor(playerLocation.getZ() - PLAYER_RADIUS);

    int x2 = (int) Math.floor(playerLocation.getX() + PLAYER_RADIUS);
    int z2 = (int) Math.floor(playerLocation.getZ() + PLAYER_RADIUS);

    long latestTick = Long.MIN_VALUE;
    Block latestBlock = null;

    for (int x = x1; x <= x2; ++x) {
      for (int z = z1; z <= z2; ++z) {
        Block block = playerLocation.getLevelBlock();
        SpleefInfo info = this.brokenBlocks.get(block);
        if (info != null) {
          long tick = info.getTime();
          if (tick > latestTick) {
            latestTick = tick;
            latestBlock = block;
          }
        }
      }
    }

    return latestBlock;
  }
}
