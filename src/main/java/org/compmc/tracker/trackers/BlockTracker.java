package org.compmc.tracker.trackers;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPistonChangeEvent;
import cn.nukkit.math.BlockFace;
import cn.nukkit.utils.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.events.world.BlockChangeEvent;
import org.compmc.tracker.info.BlockInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Tracks the ownership of {@link Block}s and resolves damage caused by them
 */
public class BlockTracker implements Listener {

  private final TrackerSupervisor supervisor;
  private final Logger logger;
  private final Map<Block, TrackerInfo> blocks = new HashMap<>();
  private final Map<Block, Integer> materials = new HashMap<>();

  public BlockTracker(TrackerSupervisor supervisor) {
    this.supervisor = supervisor;
    this.logger = TrackerPlugin.INSTANCE.getLogger();
  }

  public PhysicalInfo resolveBlock(Block block) {
    TrackerInfo info = blocks.get(block);
    if (info instanceof PhysicalInfo) {
      return (PhysicalInfo) info;
    } else if (info instanceof OwnerInfo) {
      return new BlockInfo(block.clone(), ((OwnerInfo) info).getOwner());
    } else {
      return new BlockInfo(block.clone());
    }
  }

  public @Nullable
  TrackerInfo resolveInfo(Block block) {
    return blocks.get(block);
  }

  public @Nullable
  <T extends TrackerInfo> T resolveInfo(Block block, Class<T> infoType) {
    TrackerInfo info = blocks.get(block);
    return infoType.isInstance(info) ? infoType.cast(info) : null;
  }

  public @Nullable
  TrackedPlayer getOwner(Block block) {
    OwnerInfo info = resolveInfo(block, OwnerInfo.class);
    return info == null ? null : info.getOwner();
  }

  public void trackBlockState(
      Block block, @Nullable Integer material, @Nullable TrackerInfo info) {
    checkNotNull(block);
    if (info != null) {
      blocks.put(block, info);
      if (material != null) {
        materials.put(block, material);
      } else {
        materials.remove(block);
      }
      logger.debug("Track block=" + block + " world=" + material + " info=" + info);
    } else {
      clearBlock(block);
    }
  }

  public void trackBlockState(Block state, @Nullable TrackerInfo info) {
    checkNotNull(state);
    trackBlockState(state, state.getId(), info);
  }

  public void clearBlock(Block block) {
    checkNotNull(block);
    blocks.remove(block);
    materials.remove(block);
    logger.debug("Clear block=" + block);
  }

  boolean isPlaced(Block state) {
    // If block was registered with a specific world, check that the new state
    // has the same world, otherwise assume the block is still placed.
    Integer material = materials.get(state);
    return material == null || material == state.getId();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTransform(BlockChangeEvent event) {
    if (event.getCause() instanceof BlockPistonChangeEvent) {
      return;
    }

    Block block = event.getFrom();
    TrackerInfo info = blocks.get(block);
    if (info != null && !isPlaced(event.getTo())) {
      clearBlock(block);
    }
  }

  // TODO: Figure out how to handle pistons
  private void handleMove(Collection<Block> blocks, BlockFace direction) {
    Map<Block, TrackerInfo> keepInfo = new HashMap<>();
    Map<Block, Integer> keepMaterials = new HashMap<>();
    List<Block> remove = new ArrayList<>();

    for (Block block : blocks) {
      TrackerInfo info = this.blocks.get(block);
      if (info != null) {
        remove.add(block);
        keepInfo.put(block.getSide(direction), info);

        Integer material = materials.get(block);
        if (material != null) {
          keepMaterials.put(block, material);
        }
      }
    }

    for (Block block : remove) {
      TrackerInfo info = keepInfo.remove(block);
      if (info != null) {
        this.blocks.put(block, info);

        Integer material = keepMaterials.get(block);
        if (material != null) {
          this.materials.put(block, material);
        }
      } else {
        this.blocks.remove(block);
        this.materials.remove(block);
      }
    }

    this.blocks.putAll(keepInfo);
    this.materials.putAll(keepMaterials);
  }
}
