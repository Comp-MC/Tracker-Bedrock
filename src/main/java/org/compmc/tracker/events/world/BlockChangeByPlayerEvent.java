package org.compmc.tracker.events.world;


import cn.nukkit.block.Block;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import org.compmc.tracker.player.TrackedPlayer;

/**
 * An event that is fired when any block in the world is changed by a player.
 *
 * @param <T> type of the event that caused this event to fire.
 * @author Avicus Network
 */
public class BlockChangeByPlayerEvent<T extends Event> extends BlockChangeEvent {

  private static final HandlerList handlers = new HandlerList();

  private final TrackedPlayer player;

  /**
   * Constructor.
   *
   * @param cause of the change
   * @param from state of the block before the change
   * @param to state of the block after the change
   * @param player who changed the block
   */
  public BlockChangeByPlayerEvent(
      T cause, Block from, Block to, TrackedPlayer player) {
    super(cause, from, to);
    this.player = player;
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  public TrackedPlayer getPlayer() {
    return player;
  }
}
