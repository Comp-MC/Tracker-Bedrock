package org.compmc.tracker.info;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.player.TrackedPlayer;

/**
 * Returned by the master damage resolver to indicate that the damage is invalid, i.e. because one
 * of the players involved was not participating.
 */
public class NullDamageInfo implements DamageInfo {

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return null;
  }
}
