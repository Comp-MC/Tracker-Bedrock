package org.compmc.tracker.supervisor;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.player.GenericPlayer;
import org.compmc.tracker.player.TrackedPlayer;

public class GenericSupervisor implements TrackerSupervisor {

  private final Map<UUID, GenericPlayer> players = Maps.newHashMap();

  private final Trackers trackers;

  public GenericSupervisor() {
    this.trackers = new Trackers(this);
  }

  @Override
  public TrackedPlayer getPlayer(Entity entity) {
    if (entity instanceof Player) {
      return players.get(((Player) entity).getUniqueId());
    }
    return null;
  }

  @Override
  public boolean inCombat() {
    return true;
  }

  @Override
  public Trackers getTrackers() {
    return this.trackers;
  }
}
