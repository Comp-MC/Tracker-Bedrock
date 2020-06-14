package org.compmc.tracker.events.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import cn.nukkit.level.Location;

public class PlayerCoarseMoveEvent extends PlayerEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private Location from;
  private Location to;
  private boolean cancelled;

  public PlayerCoarseMoveEvent(final Player player, Location from, Location to) {
    this.player = player;
    this.from = from;
    this.to = to;
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  public Location getFrom() {
    return this.from;
  }

  public void setFrom(Location from) {
    this.from = from;
  }

  public Location getTo() {
    return this.to;
  }

  public void setTo(Location to) {
    this.to = to;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}