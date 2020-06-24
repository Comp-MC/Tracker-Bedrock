package org.compmc.tracker.events.player;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.block.Block;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import org.compmc.tracker.info.SpleefInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class PlayerSpleefEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final TrackedPlayer victim;
  private final Block block;
  private final SpleefInfo info;

  public PlayerSpleefEvent(TrackedPlayer victim, Block block, SpleefInfo info) {
    this.victim = checkNotNull(victim);
    this.block = checkNotNull(block);
    this.info = checkNotNull(info);
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  public TrackedPlayer getVictim() {
    return victim;
  }

  public SpleefInfo getSpleefInfo() {
    return info;
  }

  public Block getBlock() {
    return block;
  }
}
