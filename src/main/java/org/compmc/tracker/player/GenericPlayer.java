package org.compmc.tracker.player;

import cn.nukkit.Player;
import java.util.UUID;

public class GenericPlayer implements TrackedPlayer {

  private final Player player;

  public GenericPlayer(Player player) {
    this.player = player;
  }

  @Override
  public Player getBase() {
    return this.player;
  }

  @Override
  public UUID getId() {
    return getBase().getUniqueId();
  }

  @Override
  public String getName() {
    return getBase().getName();
  }

  @Override
  public boolean isParticipating() {
    return true;
  }

  @Override
  public boolean isDead() {
    return !getBase().isAlive();
  }
}
