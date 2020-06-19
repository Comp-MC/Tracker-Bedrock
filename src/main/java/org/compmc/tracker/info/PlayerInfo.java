package org.compmc.tracker.info;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import org.compmc.tracker.api.info.MeleeInfo;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class PlayerInfo implements OwnerInfo, MeleeInfo, PhysicalInfo {

  private final TrackedPlayer player;
  private final ItemInfo weapon;

  public PlayerInfo(TrackedPlayer player, @Nullable ItemInfo weapon) {
    this.player = checkNotNull(player);
    this.weapon = weapon;
  }

  public PlayerInfo(TrackedPlayer player) {
    this(player, new ItemInfo(player.getBase().getInventory().getItemInHand()));
  }

  @Override
  public @Nullable
  ItemInfo getWeapon() {
    return weapon;
  }

  @Override
  public TrackedPlayer getOwner() {
    return player;
  }

  @Override
  public TrackedPlayer getAttacker() {
    return player;
  }

  @Override
  public String getIdentifier() {
    return player.getId().toString();
  }

  @Override
  public String getName() {
    return player.getName();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{player=" + getAttacker() + " weapon=" + getWeapon() + "}";
  }
}
