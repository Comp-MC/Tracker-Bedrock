package org.compmc.tracker.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockFormEvent;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.BlockSpreadEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerBucketEmptyEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import java.util.Iterator;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.events.world.BlockChangeByPlayerEvent;
import org.compmc.tracker.events.world.BlockChangeEvent;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Listener responsible for wrapping a multitude of block events into our BlockChanged_Event system
 * and handling canceling.
 */
@SuppressWarnings("JavaDoc")
public class BlockChangeListener implements Listener {

  private static final int BUCKET_WATER = 8;
  private static final int BUCKET_LAVA = 10;

  public static boolean TRACK_NATURAL_EVENTS = true;

  private final TrackerSupervisor supervisor;

  public BlockChangeListener(TrackerSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  private Block toAirState() {
    return fromMaterial(Block.AIR, 0);
  }

  private Block fromMaterial(int material, int data) {
    Block blockState = Block.get(material);
    blockState.setDamage(data);
    return blockState;
  }

  private boolean callBlockChange(Event cause, Block from, Block to) {
    BlockChangeEvent call = new BlockChangeEvent<>(cause, from, to);
    Server.getInstance().getPluginManager().callEvent(call);
    return call.isCancelled();
  }

  private boolean callBlockChange(
      Event cause, Block from, Block to, Player bukkit) {
    BlockChangeByPlayerEvent call = new BlockChangeByPlayerEvent<>(cause, from, to,
        this.supervisor.getPlayer(bukkit));
    Server.getInstance().getPluginManager().callEvent(call);
    return call.isCancelled();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockBreak(BlockBreakEvent event) {
    Block from = event.getBlock();
    Block to = toAirState();

    boolean cancel = callBlockChange(event, from, to, event.getPlayer());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockPlace(BlockPlaceEvent event) {
    Block from = event.getBlockReplace();
    Block to = event.getBlock();

    boolean cancel = callBlockChange(event, from, to, event.getPlayer());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onFromTo(BlockFromToEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }

    // Canceled water flow events can sometimes spam this
    if (event.getTo().getId() != event.getFrom().getId()) {
      Block oldState = event.getFrom();
      Block newState = event.getTo();
      callBlockChange(event, oldState, newState);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockGrow(BlockGrowEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }

    boolean cancel =
        callBlockChange(event, event.getBlock(), event.getNewState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockFade(BlockFadeEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }
    boolean cancel =
        callBlockChange(event, event.getBlock(), event.getNewState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockForm(BlockFormEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }
    boolean cancel =
        callBlockChange(event, event.getBlock(), event.getNewState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockIgnite(BlockIgniteEvent event) {
    // from TNT to TNT entity (aka AIR)
    boolean cancel =
        callBlockChange(
            event,
            event.getBlock(),
            toAirState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityExplode(EntityExplodeEvent event) {
    TrackedPlayer owner = null;
    if (event.getEntity() instanceof EntityPrimedTNT) {
      owner = this.supervisor.getTrackers().getOwner(event.getEntity());
    }

    Iterator<Block> iterator = event.getBlockList().iterator();
    while (iterator.hasNext()) {
      Block block = iterator.next();
      Block air = toAirState();

      boolean cancel = true;
      if (owner == null) {
        cancel = callBlockChange(event, block, air);
      } else {
        if (owner.getBase().isOnline() && owner.getBase() instanceof Player) {
          cancel = callBlockChange(event, block, air, owner.getBase());
        }
      }

      if (cancel) {
        iterator.remove();
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockSpread(BlockSpreadEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }

    boolean cancel =
        callBlockChange(event, event.getBlock(), event.getNewState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockBurn(BlockBurnEvent event) {
    if (!TRACK_NATURAL_EVENTS) {
      return;
    }
    boolean cancel =
        callBlockChange(
            event,
            event.getBlock(),
            toAirState());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.PHYSICAL) {
      return;
    }

    // only save the farmers
    if (event.getBlock().getId() != BlockID.DIRT) {
      return;
    }

    Block oldState = event.getBlock().clone();
    Block newState = fromMaterial(BlockID.DIRT, 0);

    boolean cancel =
        callBlockChange(event, oldState, newState, event.getPlayer());
    event.setCancelled(cancel);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event) {
    Block block = event.getBlockClicked().getSide(event.getBlockFace());

    int material;
    if (event.getBucket().getDamage() == BUCKET_LAVA) {
      material = BlockID.LAVA;
    } else if (event.getBucket().getDamage() == BUCKET_WATER) {
      material = BlockID.WATER;
    } else {
      return;
    }

    Block oldState = block.clone();
    Block newState = fromMaterial(material, 0);

    boolean cancel = callBlockChange(event, oldState, newState);
    event.setCancelled(cancel);
  }
}
