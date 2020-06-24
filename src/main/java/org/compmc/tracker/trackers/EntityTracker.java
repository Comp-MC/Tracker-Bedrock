package org.compmc.tracker.trackers;

import static com.google.common.base.Preconditions.checkNotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.entity.item.EntityPotion;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.api.info.OwnerInfo;
import org.compmc.tracker.api.info.PhysicalInfo;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.info.EntityInfo;
import org.compmc.tracker.info.FallingBlockInfo;
import org.compmc.tracker.info.MobInfo;
import org.compmc.tracker.info.PlayerInfo;
import org.compmc.tracker.info.ThrownPotionInfo;
import org.compmc.tracker.player.TrackedPlayer;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Tracks the ownership of {@link Entity}s and resolves damage caused by them
 */
public class EntityTracker implements Listener {

  private final TrackerSupervisor supervisor;
  private final Logger logger;
  private final Map<Entity, TrackerInfo> entities = new HashMap<>();

  public EntityTracker(TrackerSupervisor supervisor) {
    this.supervisor = supervisor;
    this.logger = TrackerPlugin.INSTANCE.getLogger();
  }

  public PhysicalInfo createEntity(Entity entity, @Nullable TrackedPlayer owner) {
    if (entity instanceof EntityPotion) {
      return new ThrownPotionInfo((EntityPotion) entity, owner);
    } else if (entity instanceof EntityFallingBlock) {
      return new FallingBlockInfo((EntityFallingBlock) entity, owner);
    } else if (entity instanceof EntityCreature) {
      return new MobInfo((EntityCreature) entity, owner);
    } else {
      return new EntityInfo(entity, owner);
    }
  }

  public PhysicalInfo resolveEntity(Entity entity) {
    TrackedPlayer player = this.supervisor.getPlayer(entity);
    if (player != null) {
      return new PlayerInfo(player);
    }

    TrackerInfo info = entities.get(entity);
    if (info instanceof PhysicalInfo) {
      return (PhysicalInfo) info;
    }

    TrackedPlayer owner = info instanceof OwnerInfo ? ((OwnerInfo) info).getOwner() : null;
    return createEntity(entity, owner);
  }

  public @Nullable
  TrackerInfo resolveInfo(Entity entity) {
    return entities.get(checkNotNull(entity));
  }

  public @Nullable
  <T extends TrackerInfo> T resolveInfo(Entity entity, Class<T> infoType) {
    TrackerInfo info = resolveInfo(entity);
    return infoType.isInstance(info) ? infoType.cast(info) : null;
  }

  public @Nullable
  TrackedPlayer getOwner(Entity entity) {
    if (entity instanceof Player) {
      return this.supervisor.getPlayer(entity); // Players own themselves
    } else {
      OwnerInfo info = resolveInfo(entity, OwnerInfo.class);
      return info == null ? null : info.getOwner();
    }
  }

  public void trackEntity(Entity entity, @Nullable TrackerInfo info) {
    checkNotNull(entity);
    if (info == null) {
      entities.remove(entity);
      logger.debug("Clear entity=" + entity);
    } else {
      entities.put(entity, info);
      logger.debug("Track entity=" + entity + " info=" + info);
    }
  }
}
