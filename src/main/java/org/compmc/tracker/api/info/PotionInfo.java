package org.compmc.tracker.api.info;

import cn.nukkit.potion.Effect;
import javax.annotation.Nullable;

public interface PotionInfo extends PhysicalInfo, DamageInfo {

  @Nullable
  Effect getPotionEffect();
}
