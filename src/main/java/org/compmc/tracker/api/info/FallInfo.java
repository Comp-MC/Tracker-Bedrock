package org.compmc.tracker.api.info;

public interface FallInfo extends DamageInfo, CauseInfo, RangedInfo {

  From getFrom();

  To getTo();

  enum From {
    GROUND,
    LADDER,
    WATER
  }

  enum To {
    GROUND,
    LAVA,
    VOID
  }
}
