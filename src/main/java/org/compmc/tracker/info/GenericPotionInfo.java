package org.compmc.tracker.info;

import cn.nukkit.potion.Effect;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.PotionInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class GenericPotionInfo implements PotionInfo {

  private final Effect effectType;

  public GenericPotionInfo(Effect effectType) {
    this.effectType = effectType;
  }

  @Override
  public @Nullable
  Effect getPotionEffect() {
    return effectType;
  }

  @Override
  public String getIdentifier() {
    Effect effectType = getPotionEffect();
    return effectType != null ? effectType.getName() : "EMPTY";
  }

  @Override
  public String getName() {
    return getPotionEffect().getName();
  }

  @Override
  public @Nullable
  TrackedPlayer getOwner() {
    return null;
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{potion=" + getPotionEffect() + "}";
  }
}
