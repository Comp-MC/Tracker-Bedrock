package org.compmc.tracker.trackers;

import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.events.world.BlockChangeByPlayerEvent;
import org.compmc.tracker.info.DispenserInfo;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Updates the state of owned dispensers. The ownership of dispensed things is handled by other
 * trackers.
 */
public class DispenserTracker extends AbstractTracker<DispenserInfo> {

  public DispenserTracker(Trackers trackers, TrackerSupervisor supervisor) {
    super(DispenserInfo.class, trackers, supervisor);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlace(BlockChangeByPlayerEvent event) {
    if (event.getTo().getId() == BlockID.DISPENSER) {
      blocks().trackBlockState(event.getTo(), new DispenserInfo(event.getPlayer()));
    }
  }
}
