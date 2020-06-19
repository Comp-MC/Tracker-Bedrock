package org.compmc.tracker.info;

import cn.nukkit.block.Block;
import javax.annotation.Nullable;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.player.TrackedPlayer;

public class BlockInfo extends OwnerInfoBase implements PhysicalInfo {

  private final int material;

  public BlockInfo(int material, @Nullable TrackedPlayer owner) {
    super(owner);
    this.material = material;
  }

  public BlockInfo(int material) {
    this(material, null);
  }

  public BlockInfo(Block block, @Nullable TrackedPlayer owner) {
    this(block.getId(), owner);
  }

  public BlockInfo(Block block) {
    this(block, null);
  }

  public int getMaterial() {
    return material;
  }

  @Override
  public String getIdentifier() {
    return getName();
  }

  @Override
  public String getName() {
    return Block.get(getMaterial()).getName();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{world=" + getMaterial() + " owner=" + getOwner() + "}";
  }
}
