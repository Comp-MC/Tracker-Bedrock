package org.compmc.tracker.supervisor;

import cn.nukkit.entity.Entity;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.player.TrackedPlayer;

public interface TrackerSupervisor {

  TrackedPlayer getPlayer(Entity entity);

  boolean inCombat();

  Trackers getTrackers();
}
