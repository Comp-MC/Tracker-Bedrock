package org.compmc.tracker.events.player;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;

public class PlayerOnGroundEvent extends PlayerEvent {

  private static final HandlerList handlers = new HandlerList();

  private boolean onGround;

  public PlayerOnGroundEvent(Player player, boolean onGround) {
    this.player = player;
    this.onGround = onGround;
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  public boolean getOnGround() {
    return this.onGround;
  }
}
