package org.compmc.tracker.trackers;

import cn.nukkit.block.BlockID;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.ExplosionPrimeEvent;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.events.world.BlockChangeByPlayerEvent;
import org.compmc.tracker.info.TNTInfo;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Updates the state of owned TNT blocks and entities
 */
public class TNTTracker extends AbstractTracker<TNTInfo> {

  public TNTTracker(Trackers trackers, TrackerSupervisor supervisor) {
    super(TNTInfo.class, trackers, supervisor);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlace(BlockChangeByPlayerEvent event) {
    if (event.getTo().getId() == BlockID.TNT) {
      blocks()
          .trackBlockState(
              event.getTo(),
              new TNTInfo(event.getPlayer(), event.getTo().getLocation()));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPrime(ExplosionPrimeEvent event) {
    if (event.getEntity() instanceof EntityPrimedTNT) {
      EntityPrimedTNT tnt = (EntityPrimedTNT) event.getEntity();
      if (tnt.getSource() != null && tnt.getSource() instanceof EntityPrimedTNT) {
        EntityPrimedTNT primer = (EntityPrimedTNT) tnt.getSource();
        entities().trackEntity(tnt, resolveEntity(primer));
      }
    }
  }
}
