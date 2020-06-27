package org.compmc.tracker.trackers;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.utils.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.info.DamageInfo;
import org.compmc.tracker.events.player.TrackedPlayerDeathEvent;
import org.compmc.tracker.info.GenericDamageInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * - Resolves all damage done to players and tracks the most recent one - Wraps {@link
 * PlayerDeathEvent}s in a {@link MatchPlayerDeathEvent}, together with the causing info - Displays
 * death messages
 */
public class DeathTracker implements Listener {

  private final Logger logger;
  private final Trackers trackers;
  private final TrackerSupervisor supervisor;
  private final Map<TrackedPlayer, DamageInfo> lastDamageInfos = new HashMap<>();

  public DeathTracker(Trackers trackers, TrackerSupervisor supervisor) {
    this.logger = TrackerPlugin.INSTANCE.getLogger();
    this.trackers = trackers;
    this.supervisor = supervisor;
  }

  // Trackers will do their cleanup at MONITOR level, so we listen at
  // HIGHEST to make sure all the info is still available.
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerDamage(EntityDamageEvent event) {
    TrackedPlayer victim = this.supervisor.getPlayer(event.getEntity());
    if (victim == null) {
      return;
    }

    lastDamageInfos.put(victim, trackers.resolveDamage(event));
  }

  @Nullable
  DamageInfo getLastDamage(TrackedPlayer victim) {
    DamageInfo info = lastDamageInfos.get(victim);
    if (info != null) {
      return info;
    }

    EntityDamageEvent damageEvent = victim.getBase().getLastDamageCause();
    if (damageEvent != null) {
      return trackers.resolveDamage(damageEvent);
    }

    return null;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerDeath(PlayerDeathEvent event) {
    logger.debug("Wrapping " + event);
    TrackedPlayer victim = this.supervisor.getPlayer(event.getEntity());
    if (victim == null || victim.isDead()) {
      return;
    }

    DamageInfo info = getLastDamage(victim);
    if (info == null) {
      info = new GenericDamageInfo(EntityDamageEvent.DamageCause.CUSTOM);
    }

    Server.getInstance().getPluginManager().callEvent(
        new TrackedPlayerDeathEvent(event, victim, info, CombatLogTracker.isCombatLog(event)));
  }
}
