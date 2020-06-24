package org.compmc.tracker.trackers;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.info.ProjectileInfo;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Updates the state of launched projectiles with info about the shooter. Uses {@link Entity}
 * instead of {@link cn.nukkit.item.ProjectileItem} to support custom projectiles
 */
public class ProjectileTracker extends AbstractTracker<ProjectileInfo> {

  public ProjectileTracker(Trackers trackers, TrackerSupervisor supervisor) {
    super(ProjectileInfo.class, trackers, supervisor);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    handleLaunch(event.getEntity(), event.getEntity().shootingEntity);
  }

  void handleLaunch(Entity projectile, Entity source) {
    PhysicalInfo projectileInfo = entities().resolveEntity(projectile);
    if (!(projectileInfo instanceof ProjectileInfo)
        || (((ProjectileInfo) projectileInfo).getShooter() == null && source != null)) {
      entities()
          .trackEntity(
              projectile,
              new ProjectileInfo(
                  projectileInfo,
                  trackers.resolveShooter(source),
                  projectile.getLocation(),
                  null));
    }
  }
}
