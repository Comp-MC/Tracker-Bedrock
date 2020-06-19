package org.compmc.tracker.api.info;

import javax.annotation.Nullable;

public interface CauseInfo extends TrackerInfo {

  @Nullable
  TrackerInfo getCause();
}
