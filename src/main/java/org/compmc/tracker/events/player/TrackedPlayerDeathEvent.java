package org.compmc.tracker.events.player;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerDeathEvent;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.player.TrackedPlayer;

/**
 * Called when {@link TrackedPlayer} dies, the victim is the {@link #getPlayer()}.
 */
public class TrackedPlayerDeathEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  private final TrackedPlayer victim;
  private final PlayerDeathEvent parent;
  private final DamageInfo damageInfo;
  private final boolean predicted;

  public TrackedPlayerDeathEvent(
      PlayerDeathEvent parent, TrackedPlayer victim, DamageInfo damageInfo, boolean predicted) {
    this.victim = checkNotNull(victim);
    this.parent = checkNotNull(parent);
    this.damageInfo = checkNotNull(damageInfo);
    this.predicted = predicted;
  }

  public static HandlerList getHandlers() {
    return handlers;
  }

  /**
   * Get the base {@link PlayerDeathEvent}.
   *
   * @return The parent event.
   */
  public final PlayerDeathEvent getParent() {
    return parent;
  }

  /**
   * Get victim {@link TrackedPlayer} of the {@link TrackedPlayerDeathEvent}.
   *
   * @return The victim.
   */
  public final TrackedPlayer getVictim() {
    return this.victim;
  }

  /**
   * Get the optional {@link TrackedPlayer} of the killer.
   *
   * @return The killer {@link TrackedPlayer}, or {@code null} if no killer.
   */
  public final @Nullable
  TrackedPlayer getKiller() {
    return damageInfo.getAttacker();
  }

  /**
   * Get the {@link DamageInfo} of how the death occurred.
   *
   * @return The {@link DamageInfo}.
   */
  public final DamageInfo getDamageInfo() {
    return damageInfo;
  }
}
