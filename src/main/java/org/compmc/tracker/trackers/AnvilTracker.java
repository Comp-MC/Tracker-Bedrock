package org.compmc.tracker.trackers;

import org.compmc.tracker.Trackers;
import org.compmc.tracker.info.AnvilInfo;
import org.compmc.tracker.supervisor.TrackerSupervisor;

/**
 * Updates the state of owned anvil blocks and entities.
 *
 * <p>TODO: Expand to support all falling blocks
 */
public class AnvilTracker extends AbstractTracker<AnvilInfo> {

  public AnvilTracker(Trackers trackers, TrackerSupervisor supervisor) {
    super(AnvilInfo.class, trackers, supervisor);
  }

  // TODO: Not currently possible
}
