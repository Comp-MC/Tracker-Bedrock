package org.compmc.tracker.info;

import cn.nukkit.block.Block;
import cn.nukkit.entity.item.EntityFallingBlock;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class FallingBlockInfo extends EntityInfo implements DamageInfo {

  private final int material;

  public FallingBlockInfo(EntityFallingBlock entity, @Nullable TrackedPlayer owner) {
    super(entity, owner);
    this.material = entity.getBlock();
  }

  public FallingBlockInfo(EntityFallingBlock entity) {
    this(entity, null);
  }

  @Override
  public @Nullable
  TrackedPlayer getAttacker() {
    return getOwner();
  }

  public int getMaterial() {
    return material;
  }

  @Override
  public String getName() {
    return Block.get(getMaterial()).getName();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{world="
        + getMaterial()
        + " name="
        + getCustomName()
        + " owner="
        + getOwner()
        + "}";
  }
}
