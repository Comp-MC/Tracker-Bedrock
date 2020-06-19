package org.compmc.tracker.player;

import cn.nukkit.Player;
import java.util.UUID;

public interface TrackedPlayer {

  Player getBase();

  UUID getId();

  String getName();

  boolean isParticipating();

  boolean isDead();
}
