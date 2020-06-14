package org.compmc.tracker.listeners;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Location;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.compmc.tracker.events.player.PlayerCoarseMoveEvent;
import org.compmc.tracker.events.player.PlayerOnGroundEvent;

public class MiscListener implements Listener {

  private final Map<UUID, AtomicBoolean> onGround = Maps.newHashMap();

  @EventHandler
  public void callOnGround(PlayerCoarseMoveEvent event) {
    boolean before = this.onGround
        .getOrDefault(event.getPlayer().getUniqueId(), new AtomicBoolean(true)).get();
    boolean now = event.getPlayer().isOnGround();
    if (before != now) {
      this.onGround.putIfAbsent(event.getPlayer().getUniqueId(), new AtomicBoolean());
      this.onGround.get(event.getPlayer().getUniqueId()).set(now);
      Server.getInstance().getPluginManager()
          .callEvent(new PlayerOnGroundEvent(event.getPlayer(), now));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerCoarseMoveCall(PlayerMoveEvent event) {
    Location from = event.getFrom();
    Location to = event.getTo();

    if (from.getFloorX() == to.getFloorX()) {
      if (from.getFloorY() == to.getFloorY()) {
        if (from.getFloorZ() == to.getFloorZ()) {
          return;
        }
      }
    }

    PlayerCoarseMoveEvent call = new PlayerCoarseMoveEvent(event.getPlayer(), from, to);
    call.setCancelled(event.isCancelled());

    Server.getInstance().getPluginManager().callEvent(call);

    event.setCancelled(call.isCancelled());
    event.setFrom(call.getFrom());
    event.setTo(call.getTo());
  }

  /**
   * Cancel default death message
   */
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    onGround.remove(event.getEntity().getUniqueId());
  }

  /**
   * Cancel default leave message
   */
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    onGround.remove(event.getPlayer().getUniqueId());
  }
}
