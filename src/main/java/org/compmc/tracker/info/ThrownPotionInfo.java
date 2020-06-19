package org.compmc.tracker.info;

import cn.nukkit.entity.item.EntityPotion;
import cn.nukkit.potion.Effect;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.PotionInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class ThrownPotionInfo extends EntityInfo implements PotionInfo {

  private final Effect effectType;

  public ThrownPotionInfo(EntityPotion entity, @Nullable TrackedPlayer owner) {
    super(entity, owner);
    this.effectType = entity.getEffects().get(0);
  }

  public ThrownPotionInfo(EntityPotion entity) {
    this(entity, null);
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }

  @Override
  public @Nullable
  Effect getPotionEffect() {
    return effectType;
  }

  @Override
  public String getName() {
    return getPotionEffect().getName() + " Potion";
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{type="
        + getEntityType()
        + " potion="
        + getPotionEffect()
        + "}";
  }
}
