package org.compmc.tracker.trackers;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Logger;
import javax.annotation.Nullable;
import org.compmc.tracker.TrackerPlugin;
import org.compmc.tracker.Trackers;
import org.compmc.tracker.api.info.TrackerInfo;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Base class with a few convenience methods that are useful to trackers.
 *
 * <p>Subclasses specify the type of {@link TrackerInfo} they use and the resolve methods provided
 * by the base class will filter out results of the wrong type. If subclasses don't want results to
 * be filtered, they should call the resolve methods directly on the block/entity tracker.
 */
public abstract class AbstractTracker<Info extends TrackerInfo> implements Listener {

  protected final TrackerSupervisor supervisor;
  protected final Logger logger;
  protected final Trackers trackers;
  private final Class<Info> infoClass;

  protected AbstractTracker(Class<Info> infoClass, Trackers trackers,
      TrackerSupervisor supervisor) {
    this.infoClass = infoClass;
    this.trackers = trackers;
    this.supervisor = supervisor;
    this.logger = TrackerPlugin.INSTANCE.getLogger();
  }

  protected EntityTracker entities() {
    return trackers.getEntityTracker();
  }

  protected BlockTracker blocks() {
    return trackers.getBlockTracker();
  }

  protected @Nullable
  Info resolveBlock(Block block) {
    return blocks().resolveInfo(block, infoClass);
  }

  protected @Nullable
  Info resolveEntity(Entity entity) {
    return entities().resolveInfo(entity, infoClass);
  }
}
