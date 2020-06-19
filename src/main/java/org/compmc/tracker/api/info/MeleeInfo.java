package org.compmc.tracker.api.info;

public interface MeleeInfo extends PhysicalInfo, DamageInfo {

  PhysicalInfo getWeapon();
}
