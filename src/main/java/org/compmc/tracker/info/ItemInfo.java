package org.compmc.tracker.info;

import cn.nukkit.item.Item;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class ItemInfo extends OwnerInfoBase implements PhysicalInfo {

  private static final Item AIR_STACK = new Item(0);

  private final Item item;

  public ItemInfo(@Nullable Item item, @Nullable TrackedPlayer owner) {
    super(owner);
    this.item = item != null ? item : AIR_STACK;
  }

  public ItemInfo(@Nullable Item item) {
    this(item, null);
  }

  public Item getItem() {
    return item;
  }

  @Override
  public String getIdentifier() {
    return getItem().getName();
  }

  @Override
  public String getName() {
    return getItem().getName();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{item=" + getItem() + " owner=" + getOwner() + "}";
  }
}
