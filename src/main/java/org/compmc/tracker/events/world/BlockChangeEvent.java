package org.compmc.tracker.events.world;

import cn.nukkit.block.Block;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.block.BlockEvent;

/**
 * An event that is fired when any block in the world is changed.
 *
 * @param <T> type of the event that caused this event to fire.
 * @author Avicus Network
 */
public class BlockChangeEvent<T extends Event> extends BlockEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  /**
   * The event that caused this event.
   */
  private final T cause;
  /**
   * State the block is before the change.
   */
  private final Block from;
  /**
   * State the block is after the change.
   */
  private final Block to;
  /**
   * If the event is canceled.
   */
  private boolean cancelled;

  /**
   * Constructor.
   *
   * @param cause event that caused this event
   * @param from state the block is before the change
   * @param to state the block is after the change
   */
  public BlockChangeEvent(T cause, Block from, Block to) {
    super(from);
    this.cause = cause;
    this.from = from;
    this.to = to;
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  /**
   * If the block is changed to air.
   *
   * @return if the block is changed to air
   */
  public boolean isToAir() {
    return this.to.getId() == Block.AIR;
  }

  public T getCause() {
    return cause;
  }

  public Block getFrom() {
    return from;
  }

  public Block getTo() {
    return to;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  public final boolean changedFrom(int material) {
    return from.getId() == material && to.getId() != material;
  }

  public final boolean changedTo(int material) {
    return from.getId() != material && to.getId() == material;
  }
}
