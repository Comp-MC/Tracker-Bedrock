package org.compmc.tracker.info;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.MeleeInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class MobInfo extends EntityInfo implements MeleeInfo {

  private final ItemInfo weapon;

  public MobInfo(EntityCreature mob, @Nullable TrackedPlayer owner) {
    super(mob, owner);
    this.weapon = new ItemInfo(new Item(0));
  }

  public MobInfo(EntityCreature mob) {
    this(mob, null);
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }

  @Override
  public ItemInfo getWeapon() {
    return weapon;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{entity="
        + getEntityType()
        + " name="
        + getCustomName()
        + " owner="
        + getOwner()
        + " weapon="
        + getWeapon()
        + "}";
  }
}
